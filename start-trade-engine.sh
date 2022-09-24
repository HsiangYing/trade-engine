export VERSION=$(grep '<trade-engine.version>' pom.xml | sed 's/<trade-engine.version>//g;s/<\/trade-engine.version>//g' | awk '{print $1}')

java -jar ./target/trade-engine-${VERSION}.jar