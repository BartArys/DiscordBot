package com.numbers.disko.discord

import com.numbers.disko.discord.extensions.executeAsync
import kotlinx.coroutines.experimental.Deferred
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.IShard
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.AttachmentPartEntry
import sx.blah.discord.util.Image
import sx.blah.discord.util.MessageBuilder
import sx.blah.discord.util.MessageHistory
import sx.blah.discord.util.cache.LongMap
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.util.*

interface DiscordTextChannel {
    val channel: IChannel
    val topic: String get() = channel.topic
    val id: String get() = channel.stringID
    val webhooks: MutableList<IWebhook> get() = channel.webhooks
    val usersHere: List<IUser> get() = channel.usersHere
    val userOverrides: List<PermissionOverride> get() = channel.userOverrides.values().toList()
    val isNSFW: Boolean get() = channel.isNSFW
    val messageHistory: Deferred<MessageHistory> get() = { channel.messageHistory }.executeAsync()
    val guild: IGuild get() = channel.guild

    fun getPinnedMessages() = { channel.pinnedMessages }.executeAsync()

    fun edit(name: String, position: Int, topic: String)

    fun sendFiles(vararg files: File) = { channel.sendFiles(*files) }.executeAsync()

    fun sendFiles(content: String, vararg files: File) = { channel.sendFiles(content, *files) }.executeAsync()

    fun sendFiles(embed: EmbedObject, vararg files: File) = { channel.sendFiles(embed, *files) }.executeAsync()

    fun sendFiles(content: String, vararg entries: AttachmentPartEntry) = { channel.sendFiles(content, *entries) }.executeAsync()

    fun sendFiles(embed: EmbedObject, vararg entries: AttachmentPartEntry) = { channel.sendFiles(embed, *entries) }.executeAsync()

    fun sendFiles(content: String, tts: Boolean, vararg entries: AttachmentPartEntry) = { channel.sendFiles(content, tts, *entries) }.executeAsync()

    fun sendFiles(content: String, tts: Boolean, embed: EmbedObject, vararg entries: AttachmentPartEntry) = { channel.sendFiles(content, tts, embed, *entries) }.executeAsync()

    fun copy()

    fun getMessageByID(messageID: Long) = { channel.getMessageByID(messageID) }.executeAsync()

    fun overrideUserPermissions(user: IUser, toAdd: EnumSet<Permissions>, toRemove: EnumSet<Permissions>) = { channel.overrideUserPermissions(user, toAdd, toRemove) }.executeAsync()

    fun getMessageHistory(messageCount: Int) = { channel.getMessageHistory(messageCount) }.executeAsync()

    fun sendMessage(content: String?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendMessage(embed: EmbedObject?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendMessage(content: String?, tts: Boolean): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendMessage(content: String?, embed: EmbedObject?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendMessage(content: String?, embed: EmbedObject?, tts: Boolean): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun createWebhook(name: String?): IWebhook {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun createWebhook(name: String?, avatar: Image?): IWebhook {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun createWebhook(name: String?, avatar: String?): IWebhook {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getRoleOverridesLong(): LongMap<PermissionOverride> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getFullMessageHistory(): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun mention(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getTypingStatus(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getRoleOverrides(): LongMap<PermissionOverride> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getUserOverridesLong(): LongMap<PermissionOverride> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun toggleTypingStatus() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun changePosition(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getInternalCacheCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun isPrivate(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun pin(message: IMessage?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun createInvite(maxAge: Int, maxUses: Int, temporary: Boolean, unique: Boolean): IInvite {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun changeCategory(category: ICategory?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun isDeleted(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryIn(startDate: LocalDateTime?, endDate: LocalDateTime?): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryIn(startDate: LocalDateTime?, endDate: LocalDateTime?, maxMessageCount: Int): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryIn(beginID: Long, endID: Long): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryIn(beginID: Long, endID: Long, maxMessageCount: Int): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getModifiedPermissions(user: IUser?): EnumSet<Permissions> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getModifiedPermissions(role: IRole?): EnumSet<Permissions> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getName(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getClient(): IDiscordClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getExtendedInvites(): MutableList<IExtendedInvite> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setTypingStatus(typing: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getWebhooksByName(name: String?): MutableList<IWebhook> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun unpin(message: IMessage?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getCategory(): ICategory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun changeNSFW(isNSFW: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryTo(endDate: LocalDateTime?): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryTo(endDate: LocalDateTime?, maxMessageCount: Int): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryTo(id: Long): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryTo(id: Long, maxMessageCount: Int): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun delete() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getWebhookByID(id: Long): IWebhook {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getShard(): IShard {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun removePermissionsOverride(user: IUser?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun removePermissionsOverride(role: IRole?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun changeTopic(topic: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getPosition(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun overrideRolePermissions(role: IRole?, toAdd: EnumSet<Permissions>?, toRemove: EnumSet<Permissions>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendFile(file: File?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendFile(content: String?, file: File?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendFile(embed: EmbedObject?, file: File?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendFile(content: String?, file: InputStream?, fileName: String?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendFile(embed: EmbedObject?, file: InputStream?, fileName: String?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendFile(content: String?, tts: Boolean, file: InputStream?, fileName: String?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendFile(content: String?, tts: Boolean, file: InputStream?, fileName: String?, embed: EmbedObject?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun sendFile(builder: MessageBuilder?, file: InputStream?, fileName: String?): IMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun changeName(name: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryFrom(startDate: LocalDateTime?): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryFrom(startDate: LocalDateTime?, maxMessageCount: Int): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryFrom(id: Long): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMessageHistoryFrom(id: Long, maxMessageCount: Int): MessageHistory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getMaxInternalCacheCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun bulkDelete() = { channel.bulkDelete() }.executeAsync()

    fun bulkDelete(messages: MutableList<IMessage>) = { channel.bulkDelete(messages) }.executeAsync()

}