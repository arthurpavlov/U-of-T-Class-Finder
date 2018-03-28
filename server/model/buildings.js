var mongoose = require('mongoose');
// connectionOne = require('./connectionBuildings');
var Schema = mongoose.Schema;

var BuildingsSchema = new Schema(
  {
    name: {
      type: String,
      required: true,
    },
    code: {
      type: String,
      requried: true
    },
    address: {
      type: String,
      requried: true
    }
  }
);
console.log("build");
// module.exports = connectionBuildings.model('buildings', Buildings);
var Buildings = module.exports = mongoose.model('Buildings', BuildingsSchema);
// module.exports = buildings;