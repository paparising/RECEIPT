# Spring Security & JWT Authentication - Complete Walkthrough

## Overview
This is a **stateless, token-based authentication system**. Unlike traditional login systems that use cookies and sessions, this system uses JWT tokens that the client stores and sends with every request.

---

## 🔄 Complete Authentication Flow

### **SCENARIO 1: User Registration (Signup)**

```
Client                              Server
  |                                   |
  |---1. POST /api/auth/signup------->|
  |  (username, email, password)      |
  |                                   |
  |                              AuthController.signup()
  |                                   |
  |                              Check if username exists
  |                              Check if email exists
  |                                   |
  |                              Hash password with BCrypt
  |                              Create User object
  |                              Assign USER role
  |                              Save to database
  |                                   |
  |<--2. 201 CREATED-----------------|
  |   "User registered successfully!" |
  |                                   |
```

**Code Flow:**
```java
// Step 1: User sends signup request
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}

// Step 2: AuthController receives request
@PostMapping("/signup")
public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
    // Step 3: AuthenticationService validates and creates user
    String message = authenticationService.signup(signupRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(message);
}

// Step 4: AuthenticationService.signup()
public String signup(SignupRequest signupRequest) throws Exception {
    // Check if username/email already exists
    if (userRepository.existsByUsername(signupRequest.getUsername())) {
        throw new Exception("Username is already taken!");
    }
    
    // Create new User
    User user = new User();
    user.setUsername(signupRequest.getUsername());
    user.setEmail(signupRequest.getEmail());
    
    // Step 5: Password is encrypted with BCrypt (NOT stored in plain text!)
    user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
    // Before: "securePassword123"
    // After:  "$2a$10$slYQmyNdGzin7olVN3p5Be4DlH.PKZbv5H8KnzzVgXXbVxzy58T36"
    
    // Step 6: Assign default USER role
    Set<Role> roles = new HashSet<>();
    Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new Exception("Role USER not found"));
    roles.add(userRole);
    user.setRoles(roles);
    
    // Step 7: Save to database
    userRepository.save(user);
    return "User registered successfully!";
}
```

---

### **SCENARIO 2: User Login (Authentication)**

```
Client                              Server
  |                                   |
  |---1. POST /api/auth/login-------->|
  |  (username, password)             |
  |                                   |
  |                              AuthController.login()
  |                                   |
  |                              AuthenticationService.login()
  |                              Lookup user by username
  |                              Compare passwords (BCrypt)
  |                              If valid → Generate JWT token
  |                                   |
  |<--2. 200 OK----------------------|
  |  { "token": "eyJhbGc...",         |
  |    "type": "Bearer",              |
  |    "id": 1,                       |
  |    "username": "john_doe",        |
  |    "email": "john@example.com" }  |
  |                                   |
  |  ** CLIENT STORES TOKEN **        |
  |  (in localStorage, cookie, etc.)  |
  |                                   |
```

**Code Flow:**

```java
// Step 1: User sends login credentials
{
  "username": "john_doe",
  "password": "securePassword123"
}

// Step 2: AuthController.login()
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
    try {
        // Step 3: AuthenticationService.login()
        JwtResponse jwtResponse = authenticationService.login(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }
}

// Step 4: AuthenticationService.login()
public JwtResponse login(LoginRequest loginRequest) throws AuthenticationException {
    // Step 5: Use Spring's AuthenticationManager to authenticate
    // AuthenticationManager uses DaoAuthenticationProvider
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(),    // "john_doe"
            loginRequest.getPassword()     // "securePassword123"
        )
    );
    
    // Step 6: Generate JWT token
    // This calls JwtTokenProvider.generateToken()
    String jwt = tokenProvider.generateToken(authentication);
    
    // Step 7: Fetch user details from database
    User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
    
    // Step 8: Return JWT response to client
    return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail());
}

// Step 5 DEEP DIVE: How AuthenticationManager authenticates
// 1. AuthenticationManager calls DaoAuthenticationProvider
// 2. DaoAuthenticationProvider calls CustomUserDetailsService.loadUserByUsername()

public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    // Returns UserDetails with:
    // - username
    // - encrypted password
    // - roles converted to GrantedAuthority (e.g., "ROLE_USER", "ROLE_ADMIN")
    return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())  // The encrypted password
            .accountLocked(!user.getEnabled())
            .authorities(
                user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                    .collect(Collectors.toList())
            )
            .build();
}

// 3. DaoAuthenticationProvider compares passwords
//    Input password: "securePassword123" (plain text from login)
//    Stored password: "$2a$10$slYQmyNdGzin7olVN3p5Be4DlH.PKZbv5H8KnzzVgXXbVxzy58T36" (encrypted)
//    BCrypt.matches("securePassword123", encrypted_password) → true/false

// If passwords match → Authentication success
// If passwords don't match → AuthenticationException thrown

// Step 6 DEEP DIVE: How JWT Token is Generated
public String generateToken(Authentication authentication) {
    String username = authentication.getName();  // "john_doe"
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);  // 24 hours later
    
    return Jwts.builder()
            .setSubject(username)                                   // Who (subject)
            .setIssuedAt(now)                                       // When created
            .setExpiration(expiryDate)                              // When expires
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)   // Signed with secret
            .compact();
    
    // Result: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMTk4NDUwMCwiZXhwIjoxNzAyMDcwOTAwfQ.vNk...
    //
    // JWT has 3 parts separated by dots:
    // 1. Header:    eyJhbGciOiJIUzUxMiJ9 
    //    (tells us algorithm is HS512)
    // 2. Payload:   eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMTk4NDUwMCwiZXhwIjoxNzAyMDcwOTAwfQ
    //    (contains username, issued time, expiration)
    // 3. Signature: vNk... 
    //    (proves token wasn't modified - only server knows the secret)
}
```

---

### **SCENARIO 3: Client Makes Protected Request**

```
Client                              Server
  |                                   |
  |---1. GET /api/properties/1/receipts
  |    Headers:                       |
  |    Authorization: Bearer <jwt>    |
  |                                   |
  |                              Spring Security Filter Chain:
  |                                   |
  |                              1. Request enters filter
  |                              2. JwtAuthenticationFilter runs
  |                                   |
  |                              Extract "Bearer <jwt>" from header
  |                              Validate token signature
  |                              Check if not expired
  |                              Get username from token
  |                              Load user details
  |                              Create Authentication object
  |                              Set in SecurityContext
  |                                   |
  |                              3. Check authorization rules:
  |                                 "/api/properties/**" 
  |                                 requires "USER" or "ADMIN" role
  |                                   |
  |                              4. User has USER role ✓
  |                              5. Call PropertyController
  |                                   |
  |<--2. 200 OK----------------------|
  |  { property with receipts }      |
  |                                   |
```

**Code Flow:**

```java
// Step 1: Client sends request with JWT token
GET /api/properties/1/receipts
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMTk4NDUwMCwiZXhwIjoxNzAyMDcwOTAwfQ.vNk...

// Step 2: Spring Security Filter Chain processes request
// First, JwtAuthenticationFilter runs (added before UsernamePasswordAuthenticationFilter)

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            // Step 3: Extract JWT from Authorization header
            String jwt = getJwtFromRequest(request);
            // Returns: "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMTk4NDUwMCwiZXhwIjoxNzAyMDcwOTAwfQ.vNk..."
            
            // Helper method to extract "Bearer <jwt>"
            private String getJwtFromRequest(HttpServletRequest request) {
                String bearerToken = request.getHeader("Authorization");
                // bearerToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIi..."
                
                if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                    return bearerToken.substring(7);  // Remove "Bearer " prefix
                }
                return null;
            }
            
            // Step 4: Validate token
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // validateToken() checks:
                // 1. Signature is valid (signed with our secret)
                // 2. Token is not expired
                
                // Step 5: Extract username from token
                String username = tokenProvider.getUsernameFromToken(jwt);
                // Returns: "john_doe"
                
                // Step 6: Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // Returns: User object with username, roles, etc.
                
                // Step 7: Create Authentication object
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,      // The user object
                        null,             // No credentials needed (we already validated token)
                        userDetails.getAuthorities()  // Their roles: [ROLE_USER]
                    );
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Step 8: Set authentication in SecurityContext
                // This tells Spring Security "this request is from john_doe with ROLE_USER"
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
        
        // Step 9: Continue filter chain
        filterChain.doFilter(request, response);
    }
}

// Step 10: After filter chain, Spring checks authorization rules
// From SecurityConfig:
.authorizeHttpRequests(authorizeRequests ->
    authorizeRequests
        .requestMatchers("/api/auth/**").permitAll()           // Public - no auth needed
        .requestMatchers("/api/properties/**").hasAnyRole("USER", "ADMIN")  // Requires role
        .requestMatchers("/api/receipts/**").hasAnyRole("USER", "ADMIN")    // Requires role
        .anyRequest().authenticated()                          // All other requests need authentication
)

// Checking: Is "/api/properties/1/receipts" allowed for user with [ROLE_USER]?
// Yes! It matches ".requestMatchers("/api/properties/**").hasAnyRole("USER", "ADMIN")"

// Step 11: Request allowed! Call PropertyController
@GetMapping("/{propertyId}/receipts")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // Double-check at method level
public ResponseEntity<Property> getPropertyWithReceipts(...) {
    // Execute business logic
    // Return response
}
```

---

## 🔑 Key Security Concepts

### **1. Password Storage (BCrypt)**
```
Plain text: "securePassword123"
                    ↓ BCrypt Hashing (One-way)
Stored hash: "$2a$10$slYQmyNdGzin7olVN3p5Be4DlH.PKZbv5H8KnzzVgXXbVxzy58T36"

Why BCrypt?
- One-way: Can't decrypt to get original password
- Salt: Each password gets unique salt, so same password ≠ same hash
- Slow: Takes deliberate time to compute, blocks brute-force attacks
```

### **2. JWT Token Structure**
```
Header.Payload.Signature
│      │       │
│      │       └─ Signature proves token wasn't modified
│      │
│      └─ Contains claims (data): username, issued time, expiration
│
└─ Specifies algorithm (HS512)

Example decoded payload:
{
  "sub": "john_doe",           // Subject (username)
  "iat": 1701984500,           // Issued At timestamp
  "exp": 1702070900            // Expiration timestamp (24 hours later)
}

The signature is created by:
signing_input = base64(header) + "." + base64(payload)
signature = HMAC-SHA512(signing_input, secret_key)
```

### **3. STATELESS Authentication**
```
Traditional Sessions (Stateful):
Client Request → Server creates session → Server stores in memory/database
                                    ↓
                        Subsequent requests need
                        server to lookup session

JWT Tokens (Stateless):
Client Request → Server creates JWT → Client stores token
                                    ↓
                        Subsequent requests include token
                        Server just validates without lookup
                        
Benefits:
- Scalable: Works with multiple servers (no shared session store needed)
- Mobile-friendly: Can send tokens from mobile apps
- Microservices: Each service can validate token independently
```

---

## 🔒 Security Layers

### **Layer 1: Request Enters Spring Security**
```
JwtAuthenticationFilter runs first
├─ Extracts JWT from "Authorization: Bearer <token>" header
└─ Validates token structure, signature, expiration
```

### **Layer 2: Authentication Set in Context**
```
If token valid:
├─ Extract username from JWT payload
├─ Load user details from database
├─ Create Authentication with user info and roles
└─ Set in SecurityContext (makes available throughout request)
```

### **Layer 3: Authorization Check**
```
SecurityConfig checks URL rules:
├─ "/api/auth/**" → permitAll()
├─ "/api/properties/**" → hasAnyRole("USER", "ADMIN")
├─ "/api/receipts/**" → hasAnyRole("USER", "ADMIN")
└─ Other URLs → authenticated()
```

### **Layer 4: Method-Level Authorization**
```
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<Property> getPropertyWithReceipts(...) { }

Additional check at method execution time
```

---

## 🚫 Error Scenarios

### **Scenario A: Invalid Credentials During Login**
```
Input: username="john_doe", password="wrongPassword"

1. AuthenticationManager.authenticate() is called
2. CustomUserDetailsService loads user: john_doe exists ✓
3. BCrypt compares passwords:
   - Input: "wrongPassword"
   - Stored: "$2a$10$slYQmyNdGzin7olVN3p5Be4DlH.PKZbv5H8KnzzVgXXbVxzy58T36"
   - Result: ✗ Does not match
4. BadCredentialsException thrown
5. AuthController catches exception
6. Returns: 401 Unauthorized "Invalid username or password"
```

### **Scenario B: No JWT Token in Request**
```
GET /api/properties/1/receipts
(no Authorization header)

1. JwtAuthenticationFilter extracts JWT from header
2. jwt = null (header missing)
3. StringUtils.hasText(jwt) = false → Skip validation
4. SecurityContext has no authentication
5. Spring checks authorization rules
6. "/api/properties/**" requires hasAnyRole("USER", "ADMIN")
7. Current user has no role
8. JwtAuthenticationEntryPoint.commence() called
9. Returns: 401 Unauthorized "Full authentication is required"
```

### **Scenario C: Expired JWT Token**
```
GET /api/properties/1/receipts
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...EXPIRED...

1. JwtAuthenticationFilter extracts JWT
2. tokenProvider.validateToken(jwt) is called
3. Jwts.parserBuilder()...parseClaimsJws(jwt) is called
4. Payload says: exp: 1701984500, current time: 1702071000
5. Token is expired! ExpiredJwtException thrown
6. validateToken() catches and returns false
7. Authentication NOT set
8. Spring checks authorization → Fails
9. Returns: 401 Unauthorized
```

### **Scenario D: Modified Token**
```
GET /api/properties/1/receipts
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...MODIFIED...

Client modifies token (hacker tries):
original: eyJhbGciOiJIUzUxMiJ9.eyJzdWI6Ijoi...AB123
modified: eyJhbGciOiJIUzUxMiJ9.eyJzdWI6Ijoi...HACKED

1. JwtAuthenticationFilter receives modified token
2. validateToken() is called
3. Signature verification fails (doesn't match)
4. SignatureException thrown
5. validateToken() catches and returns false
6. Authentication NOT set
7. Returns: 401 Unauthorized

Why it fails:
- Signature = HMAC-SHA512(header+payload, SECRET)
- Only server knows the SECRET
- If payload modified → signature doesn't match
- Token is rejected
```

---

## 📊 State Diagram

```
┌─────────────┐
│   START     │
└──────┬──────┘
       │
       ▼
┌──────────────────────┐
│  User visits app     │
│  (No token yet)      │
└──────┬───────────────┘
       │
       ▼
┌──────────────────────────────┐     ┌──────────────────────┐
│  Try to access protected API │────▶│  401 Unauthorized    │
│  (e.g., /properties)         │     │  (Need to login)     │
└──────┬───────────────────────┘     └──────────────────────┘
       │                                      ▲
       │                                      │
       │  Login form shown to user            │
       │  (redirects to /auth/login)          │
       │                                      │
       ▼                                      │
┌──────────────────────────────┐             │
│  POST /api/auth/login        │─────────────┘
│  username + password         │
└──────┬───────────────────────┘
       │
       ▼
┌──────────────────────────────┐
│  Server validates            │
│  credentials                 │
└──────┬───────────────────────┘
       │
       ▼
┌──────────────────────────────┐
│  ✓ Valid                     │
│  Generate JWT token          │
└──────┬───────────────────────┘
       │
       ▼
┌──────────────────────────────┐
│  Return JWT token to client  │
│  {token: "..."}              │
└──────┬───────────────────────┘
       │
       │  Client stores token
       │  (localStorage, cookie, etc.)
       │
       ▼
┌──────────────────────────────┐
│  Client ready!               │
│  Has token                   │
└──────┬───────────────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│  Request protected API with token:       │
│  GET /api/properties/1/receipts          │
│  Authorization: Bearer <token>           │
└──────┬───────────────────────────────────┘
       │
       ▼
┌──────────────────────────────┐
│  Server validates token      │
│  - Check signature           │
│  - Check expiration          │
│  - Extract username          │
└──────┬───────────────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│  ✓ Token valid                           │
│  - Set user in SecurityContext           │
│  - Check authorization rules             │
│  - User has required role?               │
└──────┬───────────────────────────────────┘
       │
       ├─── No ──────────▶ 403 Forbidden
       │
       └─── Yes
            │
            ▼
┌──────────────────────────────┐
│  ✓ Authorized!               │
│  Call controller method      │
│  Execute business logic      │
└──────┬───────────────────────┘
       │
       ▼
┌──────────────────────────────┐
│  200 OK                      │
│  Return response data        │
└──────────────────────────────┘
```

---

## 💡 Why This Architecture?

| Aspect | Benefit |
|--------|---------|
| **JWT (Stateless)** | No server session storage needed; scales easily |
| **BCrypt Passwords** | Can't decrypt; brute-force resistant |
| **Token Expiration** | Limits damage if token stolen |
| **Signature Verification** | Detects token tampering |
| **Roles/Permissions** | Fine-grained access control |
| **Filter Chain** | Centralized security logic |
| **CORS friendly** | Works with mobile and single-page apps |

---

## 🎯 Request Lifecycle Summary

```
1. Client Login
   ↓
2. Validate credentials
   ↓
3. Generate JWT (contains username, expiration)
   ↓
4. Client stores JWT
   ↓
5. Client makes request with JWT in Authorization header
   ↓
6. JwtAuthenticationFilter extracts and validates JWT
   ↓
7. Load user details from database
   ↓
8. Set Authentication in SecurityContext
   ↓
9. Check authorization rules
   ↓
10. Call controller method
    ↓
11. Return response
```

This architecture ensures:
- ✅ Only authenticated users can access protected endpoints
- ✅ Users can only do what their role allows
- ✅ Passwords are never exposed
- ✅ Tokens can't be forged or modified
- ✅ System scales without server sessions
