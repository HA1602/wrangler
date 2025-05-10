/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar Directives;

options {
  language = Java;
}

/**
 * Parser Grammar for recognizing tokens and constructs of the directives language.
 */
recipe
 : statements EOF
 ;

statements
 : ( Comment | macro | directive ';' | pragma ';' | ifStatement)*
 ;

directive
 : command
  (   codeblock
    | identifier
    | macro
    | text
    | number
    | bool
    | column
    | colList
    | numberList
    | boolList
    | stringList
    | numberRanges
    | properties
    | byteSize
    | timeDuration
  )*?
  ;

byteSize
 : BYTE_SIZE
 ;

timeDuration
 : TIME_DURATION
 ;

value
 : String | Number | Column | Bool | BYTE_SIZE | TIME_DURATION
 ;

BYTE_SIZE
 : Int BYTE_UNIT
 ;

TIME_DURATION
 : Int TIME_UNIT
 ;

fragment BYTE_UNIT
 : [gG][bB]  // gigabyte
 | [mM][bB]  // megabyte
 | [kK][bB]  // kilobyte
 | [bB]      // byte
 ;

fragment TIME_UNIT
 : [sS]       // seconds
 | [mM][sS]   // milliseconds
 ;

fragment Int
 : '-'? [1-9] Digit* [L]*
 | '0'
 ;

fragment Digit
 : [0-9]
 ;

Comment
 : ('//' ~[\r\n]* | '/*' .*? '*/' | '--' ~[\r\n]* ) -> skip
 ;

Space
 : [ \t\r\n\u000C]+ -> skip
 ;
