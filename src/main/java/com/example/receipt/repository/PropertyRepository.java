package com.example.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.receipt.entity.Property;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query("SELECT p FROM Property p LEFT JOIN FETCH p.propertyReceipts pr LEFT JOIN FETCH pr.receipt r " +
           "WHERE p.id = :propertyId " +
           "AND (:year IS NULL OR r.year = :year)")
    Optional<Property> findPropertyWithReceipts(@Param("propertyId") Long propertyId, 
                                                 @Param("year") Integer year);

    @Query("SELECT p FROM Property p LEFT JOIN FETCH p.propertyReceipts pr LEFT JOIN FETCH pr.receipt r " +
           "WHERE p.id = :propertyId")
    Optional<Property> findPropertyWithAllReceipts(@Param("propertyId") Long propertyId);
}

