/*
 * Copyright 2013 FasterXML.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package com.fasterxml.jackson.datatype.threetenbp.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;

/**
 * Deserializer for ThreeTen temporal {@link LocalTime}s.
 *
 * @author Nick Williams
 * @since 2.4.1
 */
public class LocalTimeDeserializer extends ThreeTenDateTimeDeserializerBase<LocalTime>
{
    private static final long serialVersionUID = 1L;

    public static final LocalTimeDeserializer INSTANCE = new LocalTimeDeserializer();

    private LocalTimeDeserializer() {
        this(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    protected LocalTimeDeserializer(DateTimeFormatter dtf) {
        super(LocalTime.class, dtf);

    }

    @Override
    protected JsonDeserializer<LocalTime> withDateFormat(DateTimeFormatter dtf) {
        return new LocalTimeDeserializer(dtf);
    }

    @Override
    public LocalTime deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        switch(parser.getCurrentToken())
        {
            case START_ARRAY:
                if(parser.nextToken() == JsonToken.END_ARRAY)
                    return null;
                int hour = parser.getIntValue();

                parser.nextToken();
                int minute = parser.getIntValue();

                if(parser.nextToken() != JsonToken.END_ARRAY)
                {
                    int second = parser.getIntValue();

                    if(parser.nextToken() != JsonToken.END_ARRAY)
                    {
                        int partialSecond = parser.getIntValue();
                        if(partialSecond < 1000 &&
                                !context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS))
                            partialSecond *= 1000000; // value is milliseconds, convert it to nanoseconds

                        if(parser.nextToken() != JsonToken.END_ARRAY)
                            throw context.wrongTokenException(parser, JsonToken.END_ARRAY, "Expected array to end.");

                        return LocalTime.of(hour, minute, second, partialSecond);
                    }

                    return LocalTime.of(hour, minute, second);
                }

                return LocalTime.of(hour, minute);

            case VALUE_STRING:
                String string = parser.getText().trim();
                if(string.length() == 0)
                    return null;
                return LocalTime.parse(string);
        }

        throw context.wrongTokenException(parser, JsonToken.START_ARRAY, "Expected array or string.");
    }
}
