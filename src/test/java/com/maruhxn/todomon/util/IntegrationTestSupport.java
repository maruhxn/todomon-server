package com.maruhxn.todomon.util;

import com.maruhxn.todomon.infra.file.FileService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public abstract class IntegrationTestSupport {

    @MockBean
    protected FileService fileService;

}
