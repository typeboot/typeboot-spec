#!/usr/bin/env bash

args="$@"
if [[ -z $TYPEBOOT_SPEC_FILE ]];
then
    args="$@"
else
  args="$TYPEBOOT_SPEC_FILE"
fi;
CMD="java -cp /opt/app/libs/typeboot-spec-uber.jar com.typeboot.DdlgenKt ${args}"
exec ${CMD}
