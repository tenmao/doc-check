package com.github.tenmao.check

import spock.lang.Specification

import java.time.LocalDate

/**
 *
 * @author tenmao* @since 2020/5/7
 */
class IdcardTest extends Specification {
    def "Parse"() {
        when:
        def idcard = Idcard.parse("120102200105076119")

        then:
        idcard.getNumber() == "120102200105076119"
        idcard.getBirthday() == LocalDate.of(2001, 5, 7)
        idcard.getProvince() == "天津"
        idcard.isMale()

        when:
        idcard = Idcard.parse("13030220020101122x")

        then:
        idcard.getNumber() == "13030220020101122x"
        idcard.getBirthday() == LocalDate.of(2002, 1, 1)
        idcard.getProvince() == "河北"
        !idcard.isMale()
        idcard.getAge() >= 18

        when:
        idcard = Idcard.parse("130302200201011221")

        then:
        thrown(IllegalArgumentException.class)

        when:
        idcard = Idcard.parse("1303022002010112")

        then:
        thrown(IllegalArgumentException.class)
    }
}
