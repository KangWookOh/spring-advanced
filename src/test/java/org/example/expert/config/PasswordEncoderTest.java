package org.example.expert.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class PasswordEncoderTest {

    @InjectMocks
    private PasswordEncoder passwordEncoder;
    /*
    *  PasswordEncoder 클래스에서 matches() 메소드의 순서를 준수하여
    * 원본 비밀번호를 첫 인수로 암호화된 비밀번호를 두번째 인수로 전달 하도록 테스트코드 수정
    *
    * */
    @Test
    void matches_메서드가_정상적으로_동작한다() {
        // given
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // then
        assertTrue(matches);
    }
}
