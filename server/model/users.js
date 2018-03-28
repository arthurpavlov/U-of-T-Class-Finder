var mongoose = require('mongoose');
// connectionOne = require('./connectionBuildings');
var Schema = mongoose.Schema;

var UsersSchema = new Schema(
  {
    email: {
      type: String,
      required: true,
    },
    password: {
      type: String,
      requried: true
    },
    calendar: {
      type: String,
    }
  }
);
console.log("user collection built!");
// module.exports = connectionBuildings.model('buildings', Buildings);
var Users = module.exports = mongoose.model('Users', UsersSchema);
// module.exports = buildings;