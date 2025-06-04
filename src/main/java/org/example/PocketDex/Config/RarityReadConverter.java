package org.example.PocketDex.Config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.example.PocketDex.Rarity;

@ReadingConverter
public class RarityReadConverter implements Converter<String, Rarity> {
    @Override
    public Rarity convert(String source) {
        return Rarity.fromValue(source);
    }
}
