package com.vk.api.sdk.objects.messages;

import com.google.gson.*;
import com.vk.api.sdk.objects.base.Geo;
import com.vk.api.sdk.objects.messages.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatSettingsDeserializer implements JsonDeserializer<ChatSettings> {

    @Override
    public ChatSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        
        JsonObject jsonObject = json.getAsJsonObject();
        ChatSettings settings = new ChatSettings();
        
        // Deserialize all standard fields using default Gson behavior
        if (jsonObject.has("title")) {
            settings.setTitle(jsonObject.get("title").getAsString());
        }
        if (jsonObject.has("owner_id")) {
            settings.setOwnerId(jsonObject.get("owner_id").getAsLong());
        }
        if (jsonObject.has("state")) {
            settings.setState(context.deserialize(jsonObject.get("state"), ChatSettingsState.class));
        }
        if (jsonObject.has("acl")) {
            settings.setAcl(context.deserialize(jsonObject.get("acl"), ChatSettingsAcl.class));
        }
        if (jsonObject.has("members_count")) {
            settings.setMembersCount(jsonObject.get("members_count").getAsInt());
        }
        if (jsonObject.has("admin_ids")) {
            settings.setAdminIds(parseLongArray(jsonObject.get("admin_ids").getAsJsonArray()));
        }
        if (jsonObject.has("active_ids")) {
            settings.setActiveIds(parseLongArray(jsonObject.get("active_ids").getAsJsonArray()));
        }
        if (jsonObject.has("is_group_channel")) {
            settings.setIsGroupChannel(jsonObject.get("is_group_channel").getAsBoolean());
        }
        if (jsonObject.has("is_service")) {
            settings.setIsService(jsonObject.get("is_service").getAsBoolean());
        }
        if (jsonObject.has("is_disappearing")) {
            settings.setIsDisappearing(jsonObject.get("is_disappearing").getAsBoolean());
        }
        if (jsonObject.has("disappearing_chat_link")) {
            settings.setDisappearingChatLink(jsonObject.get("disappearing_chat_link").getAsString());
        }
        if (jsonObject.has("friends_count")) {
            settings.setFriendsCount(jsonObject.get("friends_count").getAsInt());
        }
        if (jsonObject.has("pinned_messages_count")) {
            settings.setPinnedMessagesCount(jsonObject.get("pinned_messages_count").getAsInt());
        }
        if (jsonObject.has("theme")) {
            settings.setTheme(jsonObject.get("theme").getAsString());
        }
        if (jsonObject.has("photo")) {
            settings.setPhoto(context.deserialize(jsonObject.get("photo"), ChatSettingsPhoto.class));
        }
        if (jsonObject.has("permissions")) {
            settings.setPermissions(context.deserialize(jsonObject.get("permissions"), ChatSettingsPermissions.class));
        }
        
        // Custom handling for pinned_message with array attachments
        if (jsonObject.has("pinned_message")) {
            JsonElement pinnedMsgElem = jsonObject.get("pinned_message");
            if (!pinnedMsgElem.isJsonNull()) {
                PinnedMessage pinnedMessage = handlePinnedMessage(pinnedMsgElem, context);
                settings.setPinnedMessage(pinnedMessage);
            }
        }
        
        return settings;
    }
    
    private PinnedMessage handlePinnedMessage(JsonElement elem, JsonDeserializationContext context) {
        JsonObject msgObj = elem.getAsJsonObject();
        PinnedMessage message = new PinnedMessage();
        
        // Handle standard PinnedMessage fields
        if (msgObj.has("id")) {
            message.setId(msgObj.get("id").getAsInt());
        }
        if (msgObj.has("conversation_message_id")) {
            message.setConversationMessageId(msgObj.get("conversation_message_id").getAsInt());
        }
        if (msgObj.has("date")) {
            message.setDate(msgObj.get("date").getAsInt());
        }
        if (msgObj.has("from_id")) {
            message.setFromId(msgObj.get("from_id").getAsLong());
        }
        if (msgObj.has("peer_id")) {
            message.setPeerId(msgObj.get("peer_id").getAsLong());
        }
        if (msgObj.has("text")) {
            message.setText(msgObj.get("text").getAsString());
        }
        if (msgObj.has("out")) {
            message.setOut(msgObj.get("out").getAsBoolean());
        }
        if (msgObj.has("important")) {
            message.setImportant(msgObj.get("important").getAsBoolean());
        }
        if (msgObj.has("geo")) {
            message.setGeo(context.deserialize(msgObj.get("geo"), Geo.class));
        }
        if (msgObj.has("keyboard")) {
            message.setKeyboard(context.deserialize(msgObj.get("keyboard"), Keyboard.class));
        }
        if (msgObj.has("fwd_messages")) {
            message.setFwdMessages(context.deserialize(msgObj.get("fwd_messages"), 
                new com.google.gson.reflect.TypeToken<List<ForeignMessage>>(){}.getType()));
        }
        if (msgObj.has("reply_message")) {
            message.setReplyMessage(context.deserialize(msgObj.get("reply_message"), ForeignMessage.class));
        }
        
        // Special handling for attachments array
        if (msgObj.has("attachments")) {
            JsonElement attachmentsElem = msgObj.get("attachments");
            if (attachmentsElem.isJsonArray()) {
                JsonArray attachmentsArray = attachmentsElem.getAsJsonArray();
                List<MessageAttachment> attachments = parseAttachmentsArray(attachmentsArray);
                message.setAttachments(attachments);
            } else {
                // Fallback to default parsing if it's not an array
                message.setAttachments(context.deserialize(attachmentsElem, 
                    new com.google.gson.reflect.TypeToken<List<MessageAttachment>>(){}.getType()));
            }
        }
        
        return message;
    }
    
    private List<MessageAttachment> parseAttachmentsArray(JsonArray attachmentsArray) {
        List<MessageAttachment> attachments = new ArrayList<>();
        
        for (JsonElement element : attachmentsArray) {
            if (element.isJsonArray()) {
                JsonArray innerArray = element.getAsJsonArray();
                if (innerArray.size() >= 2) {
                    String type = innerArray.get(0).getAsString();
                    String data = innerArray.get(1).getAsString();
                    
                    MessageAttachment attachment = createAttachmentFromRawData(type, data, innerArray);
                    if (attachment != null) {
                        attachments.add(attachment);
                    }
                }
            }
        }
        return attachments;
    }
    
    private MessageAttachment createAttachmentFromRawData(String type, String data, JsonArray innerArray) {
        MessageAttachment attachment = new MessageAttachment();
        attachment.setType(type);
        
        // For now, we'll create a basic attachment with type information
        // You can extend this to create specific attachment types (photo, audio, etc.)
        // based on the conversion logic we discussed earlier
        
        return attachment;
    }
    
    private List<Long> parseLongArray(JsonArray jsonArray) {
        List<Long> list = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            list.add(element.getAsLong());
        }
        return list;
    }
}
