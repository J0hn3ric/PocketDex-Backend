package org.example.PocketDex.Config;

import org.example.PocketDex.Rarity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class RarityWriteConverter implements Converter<Rarity, String> {
    @Override
    public String convert(Rarity source) {
        return source.getRarityString();
    }
}
