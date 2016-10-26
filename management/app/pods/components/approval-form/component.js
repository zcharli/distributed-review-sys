import Ember from 'ember';

export default Ember.Component.extend({
  constants: Ember.inject.service("constants"),
  notifications: Ember.inject.service("notification-messages"),

  productLabel: Ember.computed('product', function () {
    if (!this.get('product.parentLevel.identifier')) {
      return false;
    }
    return `${this.get('product.parentLevel.identifier')} ${this.get("product.parentLevel.name")}`;
  }),

  productInfo: Ember.computed('product', function () {
    return this.get("product.childLevel") || this.get("product");
  }),

  dateAdded: Ember.computed('product', function () {
    return new Date(this.get('product.childLevel.created_at'));
  }),

  onDomLoad: Ember.on("didInsertElement", function () {
    Ember.$(".ui.star.rating.disabled.review").rating('disable');
  }),

  actions: {
    approveReview(approve) {
      const self = this;
      const apprv = approve ? "accept" : "deny";
      Ember.$(".ui.modal." + apprv + "." + this.get("product.childLevel.id"))
        .modal({
          closable: false,
          onDeny: function () {
            return false;
          },
          onApprove: function () {
            const data = self.get('product.childLevel');
            const url = self.get("constants.baseApi") + "/review/" + apprv + "/" + data.locationId;
            const opts = {
              url: url,
              contentType: "application/json",
              dataType: "json",
              method: "PUT",
              data: JSON.stringify(data),
            };

            Ember.$.ajax(opts).then((response) => {
              console.log("ok");
                if (response.status === "200") {
                  self.get('notifications').success("Review was updated successfully.", {
                    autoClear: true,
                    clearDuration: 2000
                  });
                  self.sendAction("action", data , apprv);
                } else {
                  self.get('notifications').warn("Query fired successfully but server returned an error.", {
                    autoClear: true,
                    clearDuration: 2000
                  });
                }
              }, (response) => {
                self.get('notifications').error("There was an error in the request.", {
                  autoClear: true,
                  clearDuration: 2000
                });
              }
            );
          }
        }).modal('show');
    }
  }
});
