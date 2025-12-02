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
    --billing-mode PAY_PER_REQUEST
```
