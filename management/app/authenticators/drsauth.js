import BaseAuthenticator from 'ember-simple-auth/authenticators/base';
import Ember from 'ember';

const {RSVP, isEmpty, run} = Ember;

export default BaseAuthenticator.extend({
  endpoint: 'http://134.117.26.135:9090',
  serverLoginEndpoint: 'http://134.117.26.135:9090' + '/api/account/login',
  serverLogoutEndpoint: 'http://134.117.26.135:9090' + '/api/account/logout',
  loggedIn: false,
  account: null,

  restore() {
    return new RSVP.Promise((resolve) => {
      // Not needed
      resolve();
    });
  },
  authenticate(identification, password, scope = []) {
    return new RSVP.Promise((resolve, reject) => {
      const data = {username: identification, password};
      const serverLoginEndpoint = this.get('serverLoginEndpoint');
      const scopesString = Ember.makeArray(scope).join(' ');
      if (!Ember.isEmpty(scopesString)) {
        data.scope = scopesString;
      }
      this.makeRequest(serverLoginEndpoint, data).then((response) => {
        run(() => {
          if (response.status === 200) {
            this.set("loggedIn", true);
            this.set("account", response.result);
            resolve(response);
          } else {
            run(null, reject, response.responseJSON || response.responseText);
          }
        });
      }, (xhr) => {
        run(null, reject, xhr.responseJSON || xhr.responseText);
      });
    });
  },
  invalidate() {
    const serverLogoutEndpoint = this.get('serverLogoutEndpoint');

    function success(resolve) {
      resolve();
    }

    return new RSVP.Promise((resolve, reject) => {
      if (!this.get("loggedIn") || this.clientId == null) {
        success.apply(this, [resolve]);
      } else {
        this.makeRequest(serverLogoutEndpoint, {
          'account': this.account
        }).then((response) => {
          run(() => {
            if (response.status === 200) {
              this.set("loggedIn", false);
              this.set("account", null);
              success.apply(this, [resolve]);
            } else {
              reject();
            }
          });
        }, (xhr) => {
          run(null, reject, xhr.responseJSON || xhr.responseText);
        });
      }
    });
  },

  makeRequest(url, data) {
    const options = {
      url: url,
      data: JSON.stringify(data),
      type: 'POST',
      dataType: 'json',
      contentType: 'application/json'
    };

    const clientIdHeader = this.get('_clientIdHeader');
    if (!isEmpty(clientIdHeader)) {
      options.headers = clientIdHeader;
    }

    return Ember.$.ajax(options);
  },
});
