<script>
	import StarRating from '$lib/components/StarRating.svelte';
	import { formatDate } from '$lib/utils/formatters.js';

	/**
	 * @typedef {Object} Experience
	 * @property {number} id
	 * @property {string} status
	 * @property {boolean} isVerified
	 * @property {string} [feedback]
	 * @property {number} [rating]
	 * @property {string} [verifiedAt]
	 * @property {number} [taskId]
	 * @property {string} [taskTitle]
	 * @property {string} [smeBusinessName]
	 * @property {number} [youthId]
	 * @property {string} [youthName]
	 * @property {string} [createdAt]
	 */

	/**
	 * @typedef {Object} Props
	 * @property {Experience} experience
	 */

	/** @type {Props} */
	let { experience } = $props();
</script>

<div
	class="bg-white border p-6 shadow-xs transition-all {experience.isVerified
		? 'border-[#7C3FE4]/40 bg-[#FAF8FF]/40'
		: 'border-[#C8D4E8]'}"
>
	<!-- Top Row: Verified Badge & Date -->
	<div class="flex items-center justify-between gap-4 mb-3">
		{#if experience.isVerified}
			<span
				class="inline-flex items-center gap-1.5 bg-[#7C3FE4] text-[#F5EEFF] px-2.5 py-0.5 text-[10px] font-mono font-bold tracking-widest uppercase"
			>
				<span>✓ VERIFIED BY SME</span>
			</span>
		{:else}
			<span
				class="inline-flex items-center gap-1.5 bg-[#F8F9FC] text-[#6A84AC] border border-[#C8D4E8] px-2 py-0.5 text-[10px] font-mono font-semibold tracking-wider uppercase"
			>
				<span>PENDING EVALUATION</span>
			</span>
		{/if}

		<span class="font-mono text-xs text-[#6A84AC]">
			{formatDate(experience.verifiedAt || experience.createdAt)}
		</span>
	</div>

	<!-- Task Title (Serif) -->
	<h4 class="font-serif text-lg font-semibold text-[#0D1B3E] leading-snug">
		{experience.taskTitle || `Task #${experience.taskId || experience.id}`}
	</h4>

	<!-- SME Business Name -->
	<p class="mt-1 text-xs font-mono text-[#1446A0]">
		{experience.smeBusinessName || 'Verified SME Partner'}
	</p>

	<!-- Star Rating & Feedback -->
	{#if experience.isVerified}
		<div class="mt-4 pt-3 border-t border-[#C8D4E8]/60">
			<div class="flex items-center justify-between mb-2">
				<span class="font-mono text-[10px] font-bold uppercase tracking-wider text-[#6A84AC]">Capability Rating</span>
				<StarRating rating={experience.rating || 5} size="sm" />
			</div>

			{#if experience.feedback}
				<div class="mt-2 border-l-2 border-[#7C3FE4] bg-[#F5EEFF]/40 p-3 text-xs text-[#0D1B3E] italic leading-relaxed font-serif">
					“{experience.feedback}”
				</div>
			{/if}
		</div>
	{:else}
		<div class="mt-4 pt-3 border-t border-[#C8D4E8] font-mono text-xs text-[#6A84AC] flex items-center gap-2">
			<span class="inline-block h-1.5 w-1.5 bg-[#D4831A]"></span>
			<span>Awaiting SME sign-off to commit entry to the ledger.</span>
		</div>
	{/if}
</div>
