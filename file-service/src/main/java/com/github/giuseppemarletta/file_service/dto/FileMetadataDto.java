package com.github.giuseppemarletta.file_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataDto {

    private String fileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String uploadDate;
    private String visibility;
}