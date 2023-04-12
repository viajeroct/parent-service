package com.viajero.mockitolearn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;

public class MockitoLearnTest {
    MockitoSession mockitoSession;

    @BeforeMethod
    public void beforeMethod() {
        mockitoSession = Mockito.mockitoSession()
                .initMocks(this)
                .startMocking();
    }

    @AfterMethod
    public void afterMethod() {
        mockitoSession.finishMocking();
    }

    @Test
    @DisplayName("test mockito")
    public void test1() {
        // это mock
        DataService dataService = Mockito.mock(DataService.class);

        // это spy
        DataService dataService1 = Mockito.spy(DataService.class);
        DataService dataService2 = Mockito.spy(dataService);

        List<String> data = new ArrayList<>();
        data.add("dataItem");

        // надо делать так:
        Mockito.when(dataService.getData()).thenReturn(data);
        Assert.assertEquals(dataService.getData(), data);

        // а можно так делать:
        Mockito.doReturn(data).when(dataService1).getData();
        Assert.assertEquals(dataService1.getData(), data);
    }

    @Test
    public void test2() {
        // если хотим для любого аргумента:
        DataService dataService = Mockito.mock(DataService.class);
        Mockito.when(dataService.getDataById(any())).thenReturn("dataItem");
        Assert.assertEquals(dataService.getDataById("1"), "dataItem");

        // если хотим для какого-то конкретного:
        DataService dataService1 = Mockito.mock(DataService.class);
        Mockito.when(dataService1.getDataById(Mockito.eq("239"))).thenReturn("cool!");
        Assert.assertEquals(dataService1.getDataById("239"), "cool!");
        Assert.assertNull(dataService1.getDataById("30"));

        // или вообще произвольный:
        DataService dataService2 = Mockito.mock(DataService.class);
        Mockito.when(dataService2.getDataById(any())).thenReturn("bad");
        Mockito.when(dataService2.getDataById(
                Mockito.argThat(arg -> arg.contains("123"))
        )).thenReturn("cool");
        Mockito.when(dataService2.getDataById("40")).thenThrow(IllegalArgumentException.class);
        Assert.assertEquals(dataService2.getDataById("9912388"), "cool");
        Assert.assertEquals(dataService2.getDataById("14"), "bad");
        Assert.assertThrows(IllegalArgumentException.class, () -> dataService2.getDataById("40"));
    }

    @Test
    public void test3() {
        DataService dataService = Mockito.mock(DataService.class);
        Mockito.when(dataService.getDataListByIds(Mockito.anyList()))
                .thenAnswer(invocationOnMock -> {
                    List<String> list = invocationOnMock.getArgument(0);
                    return list.stream().anyMatch(it -> it.contains("abc")) ? List.of("yes") : List.of("no");
                });
        Assert.assertEquals(dataService.getDataListByIds(List.of("eabcd", "kak")),
                List.of("yes"));
        Assert.assertEquals(dataService.getDataListByIds(List.of("eabdcd", "kak")),
                List.of("no"));
    }

    @Test
    public void test4() {
        DataService dataService = Mockito.mock(DataService.class);
        // первый вызов вернёт v1, второй - v2, остальные бросят исключение:
        Mockito.when(dataService.getDataById("a"))
                .thenReturn("v1", "v2")
                .thenThrow(IllegalArgumentException.class);
        Assert.assertEquals(dataService.getDataById("a"), "v1");
        Assert.assertEquals(dataService.getDataById("a"), "v2");
        Assert.assertThrows(IllegalArgumentException.class, () -> dataService.getDataById("a"));
        Assert.assertThrows(IllegalArgumentException.class, () -> dataService.getDataById("a"));
    }

    @Test
    public void test5() {
        // проверим, что метод на протяжении теста вызвали ровно 1 раз с параметром "a":
        DataService dataService = Mockito.mock(DataService.class);
        dataService.getDataById("a");
        // можно написать так:
        // Mockito.verify(dataService).getDataById("a");
        // или так:
        Mockito.verify(dataService, Mockito.times(1)).getDataById("a");

        // проверка, что это был вообще единственный метод, который вызвали на данном mock объекте
        Mockito.verify(dataService, Mockito.only()).getDataById("a");

        // проверка, что метод вызвали хотя бы 1 раз
        Mockito.verify(dataService, Mockito.atLeast(1)).getDataById("a");

        // тест падает не сразу, а в течении секунды ждёт, что вызов всё таки произойдёт
        // метод timeout завершится сразу, как будет выполнено условие, а after будет ждать millis времени
        Thread thread = new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
                dataService.getData();
            } catch (InterruptedException ignored) {
            }
        });
        thread.start();
        Mockito.verify(dataService, Mockito.after(2000).times(1)).getData();
    }

    @Test
    void test6() {
        // Mockito сохраняет историю, поэтому можно сделать несколько проверок
        DataService dataService = Mockito.mock(DataService.class);

        dataService.getDataById("a");
        dataService.getDataById("b");
        Mockito.verify(dataService, Mockito.times(2)).getDataById(Mockito.any());
        Mockito.verify(dataService, Mockito.times(1)).getDataById("a");
        Mockito.verify(dataService, Mockito.never()).getDataById("c");

        dataService.getDataById("c");
        Mockito.verify(dataService, Mockito.times(1)).getDataById("c");
        // проверим, что больше не было никаких взаимодействий, кроме указанных выше
        Mockito.verifyNoMoreInteractions(dataService);
    }

    @Test
    void test7() {
        // насчёт порядка вызовов:
        DataService dataService = Mockito.mock(DataService.class);
        InOrder inOrder = Mockito.inOrder(dataService);

        dataService.getData();
        dataService.getData();
        dataService.getDataById("a");

        // пройдёт только, если эти методы будут вызваны в нужно порядке
        inOrder.verify(dataService, Mockito.times(2)).getData();
        inOrder.verify(dataService, Mockito.times(1)).getDataById("a");
    }

    @Test
    void test8() {
        DataService dataService = Mockito.mock(DataService.class);

        // чтобы проверить вызов с более сложным аргументов нужно его перехватить:
        DataSearchRequest request = new DataSearchRequest("idValue", new Date(System.currentTimeMillis()), 50);
        dataService.getDataByRequest(request);

        ArgumentCaptor<DataSearchRequest> requestCaptor = ArgumentCaptor.forClass(DataSearchRequest.class);
        Mockito.verify(dataService, Mockito.times(1)).getDataByRequest(requestCaptor.capture());

        assertThat(requestCaptor.getAllValues()).hasSize(1);
        DataSearchRequest capturedArgument = requestCaptor.getValue();
        assertThat(capturedArgument.getId()).isNotNull();
        assertThat(capturedArgument.getId()).isEqualTo("idValue");
        assertThat(capturedArgument.getUpdatedBefore()).isAfterYear(1970);
        assertThat(capturedArgument.getLength()).isBetween(0, 100);
    }

    @Mock
    private DataService dataServiceMock;

    @Test
    void test9() {
        // этим способом все поля с аннотацией @Mock будут инициализированы моками:
        MockitoAnnotations.initMocks(this);
        Mockito.when(dataServiceMock.getData()).thenThrow(IllegalArgumentException.class);
        Assert.assertThrows(IllegalArgumentException.class, () -> dataServiceMock.getData());
    }
}
