package com.github.unafraid.spring.bot;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.groupadministration.SetChatPhoto;
import org.telegram.telegrambots.api.methods.send.SendAudio;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.send.SendSticker;
import org.telegram.telegrambots.api.methods.send.SendVideo;
import org.telegram.telegrambots.api.methods.send.SendVideoNote;
import org.telegram.telegrambots.api.methods.send.SendVoice;
import org.telegram.telegrambots.api.methods.stickers.AddStickerToSet;
import org.telegram.telegrambots.api.methods.stickers.CreateNewStickerSet;
import org.telegram.telegrambots.api.methods.stickers.UploadStickerFile;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.WebhookBot;
import org.telegram.telegrambots.updateshandlers.SentCallback;
import com.github.unafraid.spring.bot.util.BotUtil;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author UnAfraid
 */
public abstract class TelegramWebhookBot extends AbsSender implements WebhookBot {
	private final ExecutorService exe = Executors.newFixedThreadPool(1);

	@Override
	public Message sendVideoNote(SendVideoNote sendVideoNote) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public List<Message> sendMediaGroup(SendMediaGroup sendMediaGroup) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public Boolean setChatPhoto(SetChatPhoto setChatPhoto) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public Boolean addStickerToSet(AddStickerToSet addStickerToSet) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public Boolean createNewStickerSet(CreateNewStickerSet createNewStickerSet) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public File uploadStickerFile(UploadStickerFile uploadStickerFile) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public Message sendDocument(SendDocument sendDocument) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public Message sendPhoto(SendPhoto sendPhoto) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public Message sendVideo(SendVideo sendVideo) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public Message sendSticker(SendSticker sendSticker) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public Message sendAudio(SendAudio sendAudio) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	public Message sendVoice(SendVoice sendVoice) throws TelegramApiException {
		throw new NotImplementedException();
	}

	@Override
	protected <T extends Serializable, Method extends BotApiMethod<T>, Callback extends SentCallback<T>> void sendApiMethodAsync(Method method, Callback callback) {
		exe.submit(() -> {
			try {
				method.validate();

				final String result = BotUtil.doPostJSONQuery(this, method.getMethod(), method);
				try {
					callback.onResult(method, method.deserializeResponse(result));
				} catch (TelegramApiRequestException e) {
					callback.onError(method, e);
				}
			} catch (Exception e) {
				callback.onException(method, e);
			}
		});
	}

	@Override
	protected <T extends Serializable, Method extends BotApiMethod<T>> T sendApiMethod(Method method) throws TelegramApiException {
		method.validate();

		try {
			final String result = BotUtil.doPostJSONQuery(this, method.getMethod(), method);
			return method.deserializeResponse(result);
		} catch (Exception e) {
			throw new TelegramApiException("Unable to execute " + method.getMethod() + " method", e);
		}
	}
}
