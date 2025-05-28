package com.github.giuseppemarletta.file_service.Repository;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import com.github.giuseppemarletta.file_service.model.FileMetadata;

@EnableScan
public interface FileMetadataRepository extends CrudRepository<FileMetadata, String> {


}
