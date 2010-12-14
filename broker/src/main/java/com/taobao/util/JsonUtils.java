package com.taobao.util;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;

import com.google.gson.Gson;

/**
 * JsonUtils
 * 
 * @author <jushi@taobao.com>
 * @created 2010-4-20
 * 
 */
public abstract class JsonUtils {

    private final static Gson GSON = new Gson();

    public static class Parser {

        private Reader reader;

        public Parser(String json) {
            this.reader = new StringReader(json);
        }

        public Parser(Reader reader) {
            this.reader = reader;
        }

        public <T> T toAInstance(Class<T> classOfT) {
            return GSON.fromJson(reader, classOfT);
        }

        @SuppressWarnings("unchecked")
        public <T> T toAInstance(Type typeOfT) {
            return (T) GSON.fromJson(reader, typeOfT);
        }
    }

    public static String json(Object object) {
    	if(object==null) return "NULL";
        return GSON.toJson(object);
    }

    public static Parser parse(String json) {
        return new Parser(json);
    }

    public static Parser parse(Reader reader) {
        return new Parser(reader);
    }
}
