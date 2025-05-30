// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.amazonqFeatureDev

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch
import software.aws.toolkits.jetbrains.core.coroutines.disposableCoroutineScope
import software.aws.toolkits.jetbrains.core.credentials.ToolkitConnection
import software.aws.toolkits.jetbrains.core.credentials.ToolkitConnectionManagerListener
import software.aws.toolkits.jetbrains.services.amazonq.apps.AmazonQApp
import software.aws.toolkits.jetbrains.services.amazonq.apps.AmazonQAppInitContext
import software.aws.toolkits.jetbrains.services.amazonq.messages.AmazonQMessage
import software.aws.toolkits.jetbrains.services.amazonq.profile.QRegionProfile
import software.aws.toolkits.jetbrains.services.amazonq.profile.QRegionProfileSelectedListener
import software.aws.toolkits.jetbrains.services.amazonqCodeScan.auth.isCodeScanAvailable
import software.aws.toolkits.jetbrains.services.amazonqCodeTest.auth.isCodeTestAvailable
import software.aws.toolkits.jetbrains.services.amazonqDoc.auth.isDocAvailable
import software.aws.toolkits.jetbrains.services.amazonqFeatureDev.auth.isFeatureDevAvailable
import software.aws.toolkits.jetbrains.services.amazonqFeatureDev.controller.FeatureDevController
import software.aws.toolkits.jetbrains.services.amazonqFeatureDev.messages.AuthenticationUpdateMessage
import software.aws.toolkits.jetbrains.services.amazonqFeatureDev.messages.IncomingFeatureDevMessage
import software.aws.toolkits.jetbrains.services.amazonqFeatureDev.storage.ChatSessionStorage
import software.aws.toolkits.jetbrains.services.codemodernizer.utils.isCodeTransformAvailable

class FeatureDevApp : AmazonQApp {

    private val scope = disposableCoroutineScope(this)

    override val tabTypes = listOf("featuredev")

    override fun init(context: AmazonQAppInitContext) {
        val chatSessionStorage = ChatSessionStorage()
        // Create FeatureDev controller
        val inboundAppMessagesHandler =
            FeatureDevController(context, chatSessionStorage)

        context.messageTypeRegistry.register(
            "chat-prompt" to IncomingFeatureDevMessage.ChatPrompt::class,
            "new-tab-was-created" to IncomingFeatureDevMessage.NewTabCreated::class,
            "tab-was-removed" to IncomingFeatureDevMessage.TabRemoved::class,
            "auth-follow-up-was-clicked" to IncomingFeatureDevMessage.AuthFollowUpWasClicked::class,
            "follow-up-was-clicked" to IncomingFeatureDevMessage.FollowupClicked::class,
            "chat-item-voted" to IncomingFeatureDevMessage.ChatItemVotedMessage::class,
            "chat-item-feedback" to IncomingFeatureDevMessage.ChatItemFeedbackMessage::class,
            "response-body-link-click" to IncomingFeatureDevMessage.ClickedLink::class,
            "insert_code_at_cursor_position" to IncomingFeatureDevMessage.InsertCodeAtCursorPosition::class,
            "open-diff" to IncomingFeatureDevMessage.OpenDiff::class,
            "file-click" to IncomingFeatureDevMessage.FileClicked::class,
            "stop-response" to IncomingFeatureDevMessage.StopResponse::class,
            "store-code-result-message-id" to IncomingFeatureDevMessage.StoreMessageIdMessage::class
        )

        scope.launch {
            context.messagesFromUiToApp.flow.collect { message ->
                // Launch a new coroutine to handle each message
                scope.launch { handleMessage(message, inboundAppMessagesHandler) }
            }
        }

        ApplicationManager.getApplication().messageBus.connect(this).subscribe(
            ToolkitConnectionManagerListener.TOPIC,
            object : ToolkitConnectionManagerListener {
                override fun activeConnectionChanged(newConnection: ToolkitConnection?) {
                    scope.launch {
                        context.messagesFromAppToUi.publish(
                            AuthenticationUpdateMessage(
                                featureDevEnabled = isFeatureDevAvailable(context.project),
                                codeTransformEnabled = isCodeTransformAvailable(context.project),
                                codeScanEnabled = isCodeScanAvailable(context.project),
                                codeTestEnabled = isCodeTestAvailable(context.project),
                                docEnabled = isDocAvailable(context.project),
                                authenticatingTabIDs = chatSessionStorage.getAuthenticatingSessions().map { it.tabID },
                            )
                        )
                    }
                }
            }
        )

        context.project.messageBus.connect(this).subscribe(
            QRegionProfileSelectedListener.TOPIC,
            object : QRegionProfileSelectedListener {
                override fun onProfileSelected(project: Project, profile: QRegionProfile?) {
                    chatSessionStorage.deleteAllSessions()
                }
            }
        )
    }

    private suspend fun handleMessage(message: AmazonQMessage, inboundAppMessagesHandler: InboundAppMessagesHandler) {
        when (message) {
            is IncomingFeatureDevMessage.ChatPrompt -> inboundAppMessagesHandler.processPromptChatMessage(message)
            is IncomingFeatureDevMessage.NewTabCreated -> inboundAppMessagesHandler.processNewTabCreatedMessage(message)
            is IncomingFeatureDevMessage.TabRemoved -> inboundAppMessagesHandler.processTabRemovedMessage(message)
            is IncomingFeatureDevMessage.AuthFollowUpWasClicked -> inboundAppMessagesHandler.processAuthFollowUpClick(message)
            is IncomingFeatureDevMessage.FollowupClicked -> inboundAppMessagesHandler.processFollowupClickedMessage(message)
            is IncomingFeatureDevMessage.ChatItemVotedMessage -> inboundAppMessagesHandler.processChatItemVotedMessage(message)
            is IncomingFeatureDevMessage.ChatItemFeedbackMessage -> inboundAppMessagesHandler.processChatItemFeedbackMessage(message)
            is IncomingFeatureDevMessage.ClickedLink -> inboundAppMessagesHandler.processLinkClick(message)
            is IncomingFeatureDevMessage.InsertCodeAtCursorPosition -> inboundAppMessagesHandler.processInsertCodeAtCursorPosition(message)
            is IncomingFeatureDevMessage.OpenDiff -> inboundAppMessagesHandler.processOpenDiff(message)
            is IncomingFeatureDevMessage.FileClicked -> inboundAppMessagesHandler.processFileClicked(message)
            is IncomingFeatureDevMessage.StopResponse -> inboundAppMessagesHandler.processStopMessage(message)
            is IncomingFeatureDevMessage.StoreMessageIdMessage -> inboundAppMessagesHandler.processStoreCodeResultMessageId(message)
        }
    }

    override fun dispose() {
        // nothing to do
    }
}
