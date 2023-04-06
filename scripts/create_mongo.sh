export MONGODB_VERSION=6.0-ubi8
sudo docker run --name mongodb -d -p 27017:27017 mongodb/mongodb-community-server:$MONGODB_VERSION
