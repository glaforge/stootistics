<template>
  <h1>
      Mastodon Potential Reach
      <sl-icon-button name="question-circle-fill" @click="toggleHelp" style="position: relative; top: -10px; font-size: 18px" >
      </sl-icon-button>
  </h1>
  <div class="explanation" id="explanationNode" v-if="helpActive">
      <p>
          <sl-icon-button name="x-circle-fill" @click="toggleHelp" style="float: right; margin-left: 1rem; font-size: 16px" >
          </sl-icon-button>
          As there's no analytics on Mastodon, let's compute the potential reach
          your posts can get, by following the formula below, as explained in this
          <a href="https://glaforge.dev/posts/2023/01/06/calculating-your-potential-reach-on-mastodon-with-google-cloud-workflows-orchestrating-the-mastodon-apis/">article</a>:
      </p>
      <pre><code>potential_reach = <br>   me.followers_count + <br>   ∑ ( boosters[i].followers_count )</code></pre>
  </div>

  <sl-tab-group>
    <sl-tab slot="nav" panel="most-popular"
            @click="switchTab(true)">
        Account potential reach
    </sl-tab>
    <sl-tab slot="nav" panel="one-toot"
            @click="switchTab(false)">
        Toot potential reach
    </sl-tab>

    <sl-tab-panel name="most-popular">
        <div class="line">
            <sl-input id="accountInput" maxlength="255" clearable label="Mastodon account"
                      placeholder="Ex: @glaforge@uwyn.net"
                      pattern="@(\w+)@(\w+\.)+(\w{2,})"
                      required v-model="mastodonAccount">
                <sl-icon name="person" slot="prefix"></sl-icon>
            </sl-input>
        </div>
    </sl-tab-panel>
    <sl-tab-panel name="one-toot">
        <div class="line">
            <sl-input id="tootInput" maxlength="255" clearable label="Toot URL"
                      placeholder="Ex: https://uwyn.net/@glaforge/110085908005909658"
                      pattern="(https://)?(\w+\.)+(\w{2,})/@(\w+)/(\d)+"
                      required v-model="tootUrl">
                <sl-icon name="link" slot="prefix"></sl-icon>
            </sl-input>
        </div>
    </sl-tab-panel>
  </sl-tab-group>
  <div class="line">
    <sl-button @click="calculate" id="calculateBtn" variant="primary">Calculate</sl-button>
  </div>
  <br>
  <MastodonReachResult v-for="toot in toots" :toot="toot" />

  <sl-alert id="errorAlert" variant="danger" duration="3000" closable>
    <sl-icon slot="icon" name="exclamation-octagon"></sl-icon>
    <strong>An error occurred</strong> <br>
    {{ errorInformation.message }}
  </sl-alert>
</template>


<script setup>
import { ref, reactive } from 'vue';
import MastodonReachResult from "@/components/MastodonReachResult.vue";

const mastodonAccount = ref("@glaforge@uwyn.net");
const tootUrl = ref("https://uwyn.net/@glaforge/110085908005909658")

const accountTabActive = ref(true);
const helpActive = ref(true);

const toots = ref([]);

const errorInformation = reactive({message: ""});

function switchTab(activateAccount) {
    if (activateAccount) {
        accountTabActive.value = true;
        toots.value = [];
    } else {
        accountTabActive.value = false;
        toots.value = [];
    }
}

function toggleHelp() {
    helpActive.value = !helpActive.value;
}

async function calculate () {
  const alertToast = document.getElementById("errorAlert");
  const calculateBtn = document.getElementById('calculateBtn');
  const accountInput = document.getElementById('accountInput');

  let accountRequest = accountTabActive.value;

  toots.value = [];

  accountInput.reportValidity();
  if (accountInput.validity.valid) {
    calculateBtn.disabled = true;
    calculateBtn.loading = true;

    try {
      let apiEndpoint = accountRequest ? "/api/reach" : "/api/reach/toot";
      const reachResponse = await fetch(apiEndpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: accountRequest ?
            JSON.stringify({"account": mastodonAccount.value}) :
            JSON.stringify({"url": tootUrl.value})
      });

      calculateBtn.loading = false;
      calculateBtn.disabled = false;

      if (!reachResponse.ok) {
        console.log("Response not OK:", reachResponse);
        const errorResponse = await reachResponse.json();
        console.log("Error response:", errorResponse);
        errorInformation.message = `${reachResponse.status} — ${errorResponse?._embedded?.errors[0]?.message}`;
        toots.value = [];
        alertToast.show();
      } else {
        const reachResult = await reachResponse.json();
        console.log("Reach calculation", reachResult);
        toots.value = reachResult.sort((a, b) => b.reblogged - a.reblogged);
      }
    } catch (e) {
      calculateBtn.loading = false;
      calculateBtn.disabled = false;
      toots.value = [];
      console.log("Error:", e);
      errorInformation.message = e.message;
      alertToast.show();
    }
  }
}
</script>

<style scoped>
.line {
  margin-bottom: 20px;
}

sl-alert {
  position: absolute;
  right: 5rem;
  top: 5rem;
}

sl-input[data-user-invalid]::part(base) {
  border-color: var(--sl-color-danger-600);
}

[data-user-invalid]::part(form-control-label),
[data-user-invalid]::part(form-control-help-text) {
  color: var(--sl-color-danger-700);
}

sl-input:focus-within[data-user-invalid]::part(base) {
  border-color: var(--sl-color-danger-600);
  box-shadow: 0 0 0 var(--sl-focus-ring-width) var(--sl-color-danger-300);
}

sl-input[data-user-valid]::part(base) {
  border-color: var(--sl-color-success-600);
}

sl-input:focus-within[data-user-valid]::part(base) {
  border-color: var(--sl-color-success-600);
  box-shadow: 0 0 0 var(--sl-focus-ring-width) var(--sl-color-success-300);
}
</style>