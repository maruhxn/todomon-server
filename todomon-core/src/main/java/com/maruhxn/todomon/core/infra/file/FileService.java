package com.maruhxn.todomon.core.infra.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    void deleteFile(String storedFileName);

    String storeOneFile(MultipartFile file);

}
