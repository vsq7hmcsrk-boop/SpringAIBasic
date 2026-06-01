package kopo.poly.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

//10 usages  Hyeopgeon Lee
@Getter
@Setter
public class NlpDTO {

    //no usages
    private String text; // 분석할 문장

    //no usages
    private String result; // 분석된 품사 결과

    //no usages
    private List<String> nouns; // 분석된 명사 추출 결과

}
