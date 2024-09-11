package org.example.expert.domain.Todo;
import org.example.expert.client.WeatherClient;
import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class WeatherClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @InjectMocks
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // RestTemplateBuilder가 RestTemplate을 반환하도록 설정
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        // 생성자 주입 방식으로 WeatherClient 생성
        weatherClient = new WeatherClient(restTemplateBuilder);
    }

    @Test
    void getTodayWeather_성공() {
        // Arrange
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
        WeatherDto[] weatherData = {new WeatherDto(today, "Sunny")};
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(weatherData, HttpStatus.OK);
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).thenReturn(responseEntity);

        // Act
        String weather = weatherClient.getTodayWeather();

        // Assert
        assertEquals("Sunny", weather);
    }

    @Test
    void getTodayWeather_데이터없음_예외발생() {
        // Arrange
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(new WeatherDto[]{}, HttpStatus.OK);
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).thenReturn(responseEntity);

        // Act & Assert
        assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());
    }
}





