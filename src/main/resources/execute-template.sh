#!/bin/bash
$1
if [ $? -ne 0 ];then
        exit 1
else
        error=`grep -i error' '  $2 | wc -l`
        failed=`grep -i 'fail' $2 | wc -l`
        if [ $error -ne 0 ];then
            echo 'exec  error'
                exit 1
        elif [ $failed -ne 0 ];then
            echo 'fail'
                exit 1
        else
                exit 0
        fi
fi