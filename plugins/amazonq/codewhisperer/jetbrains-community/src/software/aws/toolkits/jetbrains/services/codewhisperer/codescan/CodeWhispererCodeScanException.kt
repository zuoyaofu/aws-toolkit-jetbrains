// Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.codewhisperer.codescan

import software.aws.toolkits.resources.message

open class CodeWhispererCodeScanException(override val message: String?) : RuntimeException()

open class CodeWhispererCodeFixException(override val message: String?) : RuntimeException()

open class CodeWhispererCodeScanServerException(
    override val message: String?,
    val requestId: String?,
    val requestServiceType: String?,
    val httpStatusCode: String?,
) : RuntimeException()

internal fun noFileOpenError(): Nothing =
    throw CodeWhispererCodeScanException(message("codewhisperer.codescan.no_file_open"))

internal fun codeScanFailed(errorMessage: String): Nothing =
    throw Exception(errorMessage)

internal fun cannotFindFile(errorMessage: String, filepath: String): Nothing =
    error(message("codewhisperer.codescan.file_not_found", filepath, errorMessage))

internal fun cannotFindBuildArtifacts(errorMessage: String): Nothing =
    throw CodeWhispererCodeScanException(errorMessage)

internal fun fileFormatNotSupported(format: String): Nothing =
    throw CodeWhispererCodeScanException(message("codewhisperer.codescan.file_ext_not_supported", format))

internal fun fileTooLarge(): Nothing =
    throw CodeWhispererCodeScanException(message("codewhisperer.codescan.file_too_large"))

internal fun codeScanServerException(
    errorMessage: String,
    requestId: String? = null,
    requestServiceType: String? = null,
    httpStatusCode: String? = null,
): Nothing = throw CodeWhispererCodeScanServerException(errorMessage, requestId, requestServiceType, httpStatusCode)

internal fun invalidSourceZipError(): Nothing =
    throw CodeWhispererCodeScanException(message("codewhisperer.codescan.invalid_source_zip_telemetry"))

internal fun noSupportedFilesError(): Nothing =
    throw CodeWhispererCodeScanException(message("codewhisperer.codescan.unsupported_language_error"))
