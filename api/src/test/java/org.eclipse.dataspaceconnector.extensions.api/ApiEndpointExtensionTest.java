package org.eclipse.dataspaceconnector.extensions.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;

public class ApiEndpointExtensionTest {
    String response = "Test passed!";
    ConsumerApiController consumerApiController;

    @BeforeEach
    void setUp() {
        consumerApiController = Mockito.mock(ConsumerApiController.class);
    }

    @Test
    public void testCheckHealth() {
        Mockito.when(consumerApiController.checkHealth())
                .thenReturn(response);
        Assertions.assertEquals(consumerApiController.checkHealth(), response);
    }

    @Test
    public void testSelectDataById() {
        Mockito.when(consumerApiController.selectDataById(ArgumentMatchers.any(String.class)))
                .thenReturn(response);
        Assertions.assertEquals(consumerApiController.selectDataById("1"), response);
    }

    @Test
    public void testinsertData() {
        Mockito.when(consumerApiController.insertData(ArgumentMatchers.any(String.class)))
                .thenReturn(response);
        Assertions.assertEquals(consumerApiController.insertData("data"), response);
    }
}
