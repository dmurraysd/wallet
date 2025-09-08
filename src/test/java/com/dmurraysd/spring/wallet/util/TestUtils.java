package com.dmurraysd.spring.wallet.util;

import com.dmurraysd.spring.wallet.WalletApplicationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SerializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestUtils {
    public static final ObjectMapper objectMapper = WalletApplicationConfig.objectMapper();

    public static <T> T deSerialize(final String payload,
                                    final Class<T> clazz) {
        try {
            if (Objects.nonNull(payload)) {
                return objectMapper.readValue(payload, clazz);
            }
            return null;
        } catch (IOException e) {
            throw new SerializationException("Can't deserialize data [" + payload + "]" + e.getMessage());
        }
    }

    public static <T> List<T> deSerializeToList(final String payload, final Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
