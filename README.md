# kakeibo-poc

PoC for my kakeibo app

## Setup DynamoDB on LocalStack

```console
awslocal dynamodb create-table \
    --table-name journal \
    --attribute-definitions AttributeName=aggId,AttributeType=N \
    --key-schema AttributeName=aggId,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST

awslocal dynamodb create-table \
    --table-name entry \
    --attribute-definitions AttributeName=id,AttributeType=N \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --stream-specification StreamEnabled=true,StreamViewType=NEW_AND_OLD_IMAGES
```

## Deploy Lambda 

```console
awslocal lambda create-function \
    --function-name kakeibo-bs \
    --runtime java21 \
    --handler kakeibo.bs.Handler \
    --zip-file fileb://apps/bs/target/scala-3.7.4/kakeibo-bs-assembly-0.1.0-SNAPSHOT.jar \
    --role arn:aws:iam::000000000000:role/lambda-role \
    --memory-size 512 \
    --timeout 900
```

```console
awslocal lambda update-function-code \
    --function-name kakeibo-bs \
    --zip-file fileb://apps/bs/target/scala-3.7.4/kakeibo-bs-assembly-0.1.0-SNAPSHOT.jar
```

## Invoke Lambda manually

```console
awslocal lambda invoke \
    --function-name kakeibo-bs \
    --payload file://apps/bs/event.json \
    --cli-binary-format raw-in-base64-out output.txt
```

## Add event source mapping

```console
awslocal lambda create-event-source-mapping \
    --function-name kakeibo-bs \
    --event-source arn:aws:dynamodb:us-east-1:000000000000:table/entry/stream/2025-12-02T07:46:56.534  \
    --batch-size 1 \
    --starting-position TRIM_HORIZON
```

```console
awslocal logs tail /aws/lambda/kakeibo-bs \
    --follow
```