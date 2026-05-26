package com.bijlipay.repository;

import com.bijlipay.model.Category;
import com.bijlipay.model.UploadedDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UploadedDocumentsRepository extends JpaRepository<UploadedDocuments, Long> {
    @Query(value = "SELECT u FROM UploadedDocuments u WHERE u.category =?1 ")
    List<UploadedDocuments> findUploadedDocumentCount(Category leadDocuments);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM UploadedDocuments u WHERE u.category =?1")
    void findAndDeleteLeadDocument(Category leadDocument);
}
