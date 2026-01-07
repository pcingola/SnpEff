FROM alpine:3.20 as builder
ENV MAVEN_VERSION="3.9.9"
ENV SNPEFF_VERSION="5.2"

RUN apk update \
  && apk upgrade \
  && apk add --update openjdk21 tzdata curl unzip bash \
  && rm -rf /var/cache/apk/*

# Maven install
# If you are reading this, etiher:
# 2 years has passed and apache dropped support for maven 3.9.9, check what is current version and update it.
# Please update maven and jdk to new versions and update this message.
RUN curl -L https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz -O
RUN tar -zxvf apache-maven-$MAVEN_VERSION-bin.tar.gz
ENV PATH="$PATH:/apache-maven-$MAVEN_VERSION/bin"

COPY . /SnpEff

WORKDIR "SnpEff"

RUN mvn clean compile assembly:single jar:jar

RUN mv target/SnpEff-$SNPEFF_VERSION-jar-with-dependencies.jar target/SnpEff-jar-with-dependencies.jar

# Actual runtime
FROM alpine:3.20 AS runtime

RUN apk update \
  && apk upgrade \
  && apk add --update openjdk21 tzdata curl unzip bash \
  && rm -rf /var/cache/apk/*

COPY --from=builder /SnpEff/target/SnpEff-jar-with-dependencies.jar /snpEff/snpEff.jar
COPY --from=builder /SnpEff/scripts/snpEff /snpEff/scripts/
COPY --from=builder /SnpEff/config /snpEff/config
COPY --from=builder /SnpEff/snpEff.config /snpEff/snpEff.config

ENV PATH="$PATH:/snpEff/scripts"
WORKDIR /snpEff

