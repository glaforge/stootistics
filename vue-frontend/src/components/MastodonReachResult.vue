<template>

  <div class="tootCard">

    <sl-tooltip>
      <div slot="content">
        <ul>
          <li v-for="reb in toot?.rebloggedBy">{{ reb }}</li>
        </ul>
      </div>
      <sl-tag variant="primary" size="large" pill>
        <sl-icon name="recycle" slot="prefix"></sl-icon>
        <strong>{{ toot.reblogged + toot.followersCount}} reached</strong> &nbsp;
        <span v-if="toot.reblogged > 0">
          with <strong>{{ toot.rebloggedBy?.length }} reboosts</strong> &nbsp;
          <em>(+{{ toot.reblogged }})</em>
        </span>
      </sl-tag>
    </sl-tooltip>
    <sl-tag variant="success" size="large" pill v-if="toot.favorites > 0">
      <sl-icon name="star" slot="prefix"></sl-icon>
      <strong>{{ toot.favorites }} favorites</strong>
    </sl-tag>
    <div class="link">
      <sl-icon name="link" slot="prefix"></sl-icon> &nbsp;
      <a :href="toot.status.url" target="_blank">
        {{ toot.status.url.substring(8) }}
      </a>
    </div>
    <div class="content" v-html="toot.status.content"></div>
  </div>
</template>

<script setup>
defineProps(['toot'])
</script>

<style scoped>
sl-tooltip div ul {
  margin: 0;
  padding: 0;
}

.tootCard {
  /*
  */
  box-shadow: 0.25rem 0.25rem 1rem lightgray;
  border: 1px solid gray;
  border-radius: 1rem;
  padding: 1rem;
  margin-bottom: 2rem;
}

sl-button {
  margin-right: 1rem;
}

.link {
  margin-top: 1rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.content {
  background-color: var(--sl-color-gray-100);
  font-size: smaller;
  font-style: italic;
  padding: 0.1rem 1rem 0.1rem 1rem;
  border-radius: 1rem;
  margin-top: 1rem;
}

.content p {
}
</style>