package com._37coins.plivo;

import java.io.IOException;
import java.io.Writer;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

public class XmlCharacterHandler implements CharacterEscapeHandler {

	public void escape(char[] buf, int start, int len, boolean isAttValue,
			Writer out) throws IOException {
		for (int i = start; i < start+len; i++) {
			if (buf[i] < 0x10){
				//do nothing
			}else if (buf[i] < 0x20 || buf[i] > 0x7f) {
				out.write("&#");
				out.write(String.valueOf((int) buf[i]));
				out.write(";");
			} else {
				out.write(buf[i]);
			}
		}

	}

}
