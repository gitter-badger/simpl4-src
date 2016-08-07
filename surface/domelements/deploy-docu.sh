#!/bin/sh

node hydrolysis.js  >/tmp/descriptor.json

scp /tmp/descriptor.json  ms:$simpl4first/gitrepos/cassandra/docu/en/customelements-descriptor.json
