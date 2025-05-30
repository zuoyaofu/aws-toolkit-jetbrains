// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.codemodernizer

import software.aws.toolkits.jetbrains.services.codemodernizer.commands.CodeTransformActionMessage
import software.aws.toolkits.jetbrains.services.codemodernizer.messages.IncomingCodeTransformMessage

interface InboundAppMessagesHandler {
    suspend fun processTransformQuickAction(message: IncomingCodeTransformMessage.Transform)

    suspend fun processCodeTransformCancelAction(message: IncomingCodeTransformMessage.CodeTransformCancel)

    suspend fun processCodeTransformContinueAction(message: IncomingCodeTransformMessage.CodeTransformContinue)

    suspend fun processCodeTransformStartAction(message: IncomingCodeTransformMessage.CodeTransformStart)

    suspend fun processCodeTransformSelectSQLMetadataAction(message: IncomingCodeTransformMessage.CodeTransformSelectSQLMetadata)

    suspend fun processCodeTransformSelectSQLModuleSchemaAction(message: IncomingCodeTransformMessage.CodeTransformSelectSQLModuleSchema)

    suspend fun processCodeTransformCustomDependencyVersions(message: IncomingCodeTransformMessage.CodeTransformConfirmCustomDependencyVersions)

    suspend fun processCodeTransformStopAction(tabId: String)

    suspend fun processChatPromptMessage(message: IncomingCodeTransformMessage.ChatPrompt)

    suspend fun processCodeTransformConfirmSkipTests(message: IncomingCodeTransformMessage.CodeTransformConfirmSkipTests)

    suspend fun processCodeTransformOpenTransformHub(message: IncomingCodeTransformMessage.CodeTransformOpenTransformHub)

    suspend fun processCodeTransformOpenMvnBuild(message: IncomingCodeTransformMessage.CodeTransformOpenMvnBuild)

    suspend fun processCodeTransformViewDiff(message: IncomingCodeTransformMessage.CodeTransformViewDiff)

    suspend fun processCodeTransformViewSummary(message: IncomingCodeTransformMessage.CodeTransformViewSummary)

    suspend fun processCodeTransformViewBuildLog(message: IncomingCodeTransformMessage.CodeTransformViewBuildLog)

    suspend fun processCodeTransformNewAction(message: IncomingCodeTransformMessage.CodeTransformNew)

    suspend fun processCodeTransformCommand(message: CodeTransformActionMessage)

    suspend fun processTabCreated(message: IncomingCodeTransformMessage.TabCreated)

    suspend fun processTabRemoved(message: IncomingCodeTransformMessage.TabRemoved)

    suspend fun processAuthFollowUpClick(message: IncomingCodeTransformMessage.AuthFollowUpWasClicked)

    suspend fun processBodyLinkClicked(message: IncomingCodeTransformMessage.BodyLinkClicked)

    suspend fun processConfirmHilSelection(message: IncomingCodeTransformMessage.ConfirmHilSelection)

    suspend fun processRejectHilSelection(message: IncomingCodeTransformMessage.RejectHilSelection)

    suspend fun processOpenPomFileHilClicked(message: IncomingCodeTransformMessage.OpenPomFileHilClicked)
}
