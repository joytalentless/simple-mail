package org.simplejavamail.internal.clisupport.valueinterpreters;

import org.simplejavamail.converter.EmailConverter;

import org.jetbrains.annotations.NotNull;
import javax.mail.internet.MimeMessage;
import java.io.File;

public class MsgFilePathToMimeMessageFunction extends FileBasedFunction<MimeMessage> {
	
	@Override
	public Class<String> getFromType() {
		return String.class;
	}
	
	@Override
	public Class<MimeMessage> getTargetType() {
		return MimeMessage.class;
	}
	
	@NotNull
	@Override
	protected MimeMessage convertFile(File msgFile) {
		return EmailConverter.outlookMsgToMimeMessage(msgFile);
	}
}
