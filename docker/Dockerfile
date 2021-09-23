FROM kcoinprotocol/kcoin-gradle

RUN set -o errexit -o nounset \
#  Download and build java-kcoin
    && echo "git clone" \
    && git clone https://github.com/kcoinprotocol/java-kcoin.git \
    && cd java-kcoin \
    && gradle build

# Change work directory
WORKDIR /java-kcoin

# open port 18888
EXPOSE 18888

