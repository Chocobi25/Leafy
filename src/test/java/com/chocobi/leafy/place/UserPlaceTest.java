package com.chocobi.leafy.place;

import com.chocobi.leafy.place.dto.UserPlaceDTO;
import com.chocobi.leafy.place.entity.Place;
import com.chocobi.leafy.place.entity.Type;
import com.chocobi.leafy.place.repository.PlaceRepository;
import com.chocobi.leafy.place.service.PlaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserPlaceTest {
    @Mock
    private PlaceRepository userPlaceRepository;

    @InjectMocks
    private PlaceService placeService;

    @Captor
    private ArgumentCaptor<Place> userPlaceCaptor;

    @Test
    @DisplayName("이미 같은 제목이 존재하면 기존 id 를 반환하고 save 를 호출하지 않는다")
    void saveUserPlace_returnExistingId_whenTitleAlreadyExists() {
        // given
        Long existingId = 1L;
        Place existing = mock(Place.class);
        when(existing.getId()).thenReturn(existingId);

        UserPlaceDTO dto = mock(UserPlaceDTO.class);
        when(dto.getTitle()).thenReturn("My Place");

        when(userPlaceRepository.findByTitle("My Place"))
                .thenReturn(Optional.of(existing));

        // when
        Long id = placeService.saveUserPlace(dto);

        // then
        assertThat(id).isEqualTo(existingId);
        verify(userPlaceRepository, times(1)).findByTitle("My Place");
        verify(userPlaceRepository, never()).save(any(Place.class));
    }

    @Test
    @DisplayName("같은 제목이 없다면 새로 저장하고 생성된 id 를 반환한다 (type=USER 로 저장)")
    void saveUserPlace_saveAndReturnNewId_whenNotExists() {
        // given
        UserPlaceDTO dto = mock(UserPlaceDTO.class);
        when(dto.getTitle()).thenReturn("New Place");
        when(dto.getAddress()).thenReturn("Seoul");
        when(dto.getLatitude()).thenReturn(37.1234);
        when(dto.getLongitude()).thenReturn(127.5678);
        when(dto.getPlaceUrl()).thenReturn("http://kakao.map/some");

        when(userPlaceRepository.findByTitle("New Place"))
                .thenReturn(Optional.empty());

        // save 호출 시 id 가 null 이라서, Mockito answer 로 id 를 주입
        when(userPlaceRepository.save(any(Place.class)))
                .thenAnswer(invocation -> {
                    Place p = invocation.getArgument(0);
                    ReflectionTestUtils.setField(p, "id", 100L);
                    return p;
                });

        // when
        Long id = placeService.saveUserPlace(dto);

        // then
        assertThat(id).isEqualTo(100L);

        verify(userPlaceRepository).findByTitle("New Place");
        verify(userPlaceRepository).save(userPlaceCaptor.capture());

        Place saved = userPlaceCaptor.getValue();
        assertThat(saved.getTitle()).isEqualTo("New Place");
        assertThat(saved.getAddress()).isEqualTo("Seoul");
        assertThat(saved.getLatitude()).isEqualTo(37.1234);
        assertThat(saved.getLongitude()).isEqualTo(127.5678);
        assertThat(saved.getUrl()).isEqualTo("http://kakao.map/some"); // 게터 이름에 맞게 수정 필요
        assertThat(saved.getType()).isEqualTo(Type.USER);
    }
}
