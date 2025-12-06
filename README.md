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
    --table-name event \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --stream-specification StreamEnabled=true,StreamViewType=NEW_AND_OLD_IMAGES
```

## Scan tables

```console
awslocal dynamodb scan --table-name journal
```

```console
awslocal dynamodb scan --table-name event
```

## Deploy Lambda 

### Create

```console
awslocal lambda create-function \
    --function-name kakeibo-financial-statement \
    --runtime java21 \
    --handler kakeibo.financial_statement.Handler \
    --zip-file fileb://apps/financial-statement/target/scala-3.7.4/kakeibo-financial-statement-assembly-0.1.0-SNAPSHOT.jar  \
    --role arn:aws:iam::000000000000:role/lambda-role \
    --memory-size 512 \
    --timeout 900
```

### Update

```console
awslocal lambda update-function-code \
    --function-name kakeibo-financial-statement \
    --zip-file fileb://apps/bs/target/scala-3.7.4/kakeibo-financial-statement-assembly-0.1.0-SNAPSHOT.jar 
```

## Invoke Lambda manually

```console
awslocal lambda invoke \
    --function-name kakeibo-financial-statement \
    --payload file://apps/financial-statement/event.json \
    --cli-binary-format raw-in-base64-out output.txt
```

## Describe table

```console
awslocal dynamodb describe-table --table-name event
```

## Add event source mapping

```console
awslocal lambda create-event-source-mapping \
    --function-name kakeibo-financial-statement \
    --event-source arn:aws:dynamodb:us-east-1:000000000000:table/event/stream/2025-12-06T15:23:15.176  \
    --batch-size 1 \
    --starting-position TRIM_HORIZON
```

```console
awslocal logs tail /aws/lambda/kakeibo-financial-statement \
    --follow
```