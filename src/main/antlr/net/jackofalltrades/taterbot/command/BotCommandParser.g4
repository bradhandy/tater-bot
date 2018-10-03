parser grammar BotCommandParser;

options {
    language = Java;
    tokenVocab = 'net/jackofalltrades/taterbot/command/BotCommandLexer';
  }

@header {
package net.jackofalltrades.taterbot.command;
  }

command:
    (raw_command | prefixed_command)
    SPACE* UNKNOWN_SYMBOLS*
    EOF;

raw_command: (help_command | record_command | service_command);
prefixed_command: PREFIX SPACE+ raw_command;

help_command: HELP;
record_command: RECORD SPACE+ (record_action);
service_command: SERVICE SPACE+ ((service_action SPACE+ service_type) | service_list_action | service_help_action);

non_help_command: RECORD;

record_action: START_ACTION | STOP_ACTION | HELP;

service_action: DISABLE_ACTION | ENABLE_ACTION;
service_help_action: HELP;
service_list_action: LIST_ACTION;

service_type: non_help_command | SNARK;