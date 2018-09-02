lexer grammar BotCommandLexer;

options {
    language = Java;
  }

@header {
package net.jackofalltrades.taterbot.command;
  }

UNKNOWN_SYMBOLS: ~' ' ;

SPACE: ' ';

PREFIX: [Tt] [Aa] [Tt] [Ee] [Rr] [Bb] [Oo] [Tt];

// command names
HELP: [Hh] [Ee] [Ll] [Pp];
RECORD: [Rr] [Ee] [Cc] [Oo] [Rr] [Dd];
SERVICE: [Ss] [Ee] [Rr] [Vv] [Ii] [Cc] [Ee];

// record command actions.
START_ACTION: [Ss] [Tt] [Aa] [Rr] [Tt];
STOP_ACTION: [Ss] [Tt] [Oo] [Pp];

// service command actions.
DISABLE_ACTION: [Dd] [Ii] [Ss] [Aa] [Bb] [Ll] [Ee];
ENABLE_ACTION: [Ee] [Nn] [Aa] [Bb] [Ll] [Ee];
LIST_ACTION: [Ll] [Ii] [Ss] [Tt];

// service types.
SNARK: [Ss] [Nn] [Aa] [Rr] [Kk];
