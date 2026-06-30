<script lang="ts" setup>
import { isExternal } from "@@/utils/validate"

interface Props {
  to: string
}

const props = defineProps<Props>()
</script>

<template>
  <a :href="props.to" rel="noopener">
    <slot />
  </a>
</template>
