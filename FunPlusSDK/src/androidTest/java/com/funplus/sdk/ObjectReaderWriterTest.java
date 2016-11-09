package com.funplus.sdk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ObjectReaderWriterTest {

    private static final String FILENAME = "test_object";

    @Test
    public void testReadWrite() {
        // Given
        FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();
        Context context = InstrumentationRegistry.getContext();
        List<String> data = new ArrayList<>();
        data.add("log1");
        data.add("log2");
        ObjectReaderWriter.writeObject(data, funPlusConfig, FILENAME, "data");

        // When
        @SuppressWarnings("unchecked")
        List<String> retrieved = ObjectReaderWriter.readObject(funPlusConfig, FILENAME, "data", List.class);

        // Then
        assertEquals(data, retrieved);
        assertEquals(2, retrieved.size());
        assertEquals("log1", retrieved.get(0));
        assertEquals("log2", retrieved.get(1));
    }
}
