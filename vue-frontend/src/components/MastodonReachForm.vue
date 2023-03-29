<template>
  <div class="line">
    <sl-input id="accountInput" maxlength="255" clearable label="Mastodon account"
              placeholder="Ex: @glaforge@uwyn.net" pattern="@(\w+)@(\w+\.)+(\w{2,})"
              validationMessage="Account names are of the form @username@server.net"
              required v-model="mastodonAccount">
      <sl-icon name="person" slot="prefix"></sl-icon>
    </sl-input>
  </div>
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
const toots = ref([]);
const errorInformation = reactive({message: ""});

async function calculate () {
  const alertToast = document.getElementById("errorAlert");
  const calculateBtn = document.getElementById('calculateBtn');
  const accountInput = document.getElementById('accountInput');

  toots.value = [];

  accountInput.reportValidity();
  if (accountInput.validity.valid) {
    calculateBtn.disabled = true;
    calculateBtn.loading = true;

    try {
      const reachResponse = await fetch("/api/reach", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({"account": mastodonAccount.value})
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
        toots.value = reachResult;
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