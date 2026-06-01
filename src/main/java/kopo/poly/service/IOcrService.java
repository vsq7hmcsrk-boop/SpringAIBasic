package kopo.poly.service;

import kopo.poly.dto.OcrDTO;

public interface IOcrService {

    String modelFile = "C:/model/tessdata";

    OcrDTO getReadforImageText(OcrDTO pDTO) throws Exception;
}
