package net.pengtul.servsync_api.API


// Here we are defining the possible responses for Sender
// OK => The request was understood, acknowledged, performed, and successful.
// MALFORMED_REQUEST => Parsing of the JSON failed.
// REQUEST_FAILED_PERMS => Insufficient Permissions, used for RCON
// REQUEST_FAILED_OTHER_ERROR => Some other error occurred (server side, not this plugin)
// REQUEST_FAILED_PLUGIN => This plugin itself encountered an unexpected exception and we don't know how to handle it


public enum class Status {
    OK,
    MALFORMED_REQUEST,
    REQUEST_FAILED_PERMS,
    REQUEST_FAILED_OTHER_ERROR,
    REQUEST_FAILED_PLUGIN
}