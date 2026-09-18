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
	class="relative rounded-2xl border {experience.isVerified
		? 'border-purple-200/90 bg-gradient-to-br from-white to-purple-50/30'
		: 'border-slate-200 bg-white'} p-6 shadow-xs transition-all hover:shadow-md"
>
	<!-- Top Row: Verified Badge & Date -->
	<div class="flex items-center justify-between gap-4 mb-3">
		{#if experience.isVerified}
			<span
				class="inline-flex items-center gap-1.5 rounded-full bg-purple-50 px-3 py-1 text-xs font-semibold text-purple-700 border border-purple-200/70"
			>
				<svg class="h-4 w-4 text-purple-600" viewBox="0 0 20 20" fill="currentColor">
					<path
						fill-rule="evenodd"
						d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z"
						clip-rule="evenodd"
					/>
				</svg>
				<span>Verified by SME</span>
			</span>
		{:else}
			<span
				class="inline-flex items-center gap-1.5 rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600 border border-slate-200"
			>
				<svg class="h-3.5 w-3.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
				</svg>
				<span>Pending SME Verification</span>
			</span>
		{/if}

		<span class="text-xs text-slate-400 font-medium">
			{formatDate(experience.verifiedAt || experience.createdAt)}
		</span>
	</div>

	<!-- Task Title -->
	<h4 class="text-base font-bold text-slate-900 leading-snug">
		{experience.taskTitle || `Task #${experience.taskId || experience.id}`}
	</h4>

	<!-- SME Business Name -->
	<p class="mt-1 text-xs font-semibold text-indigo-600 flex items-center gap-1.5">
		<span>{experience.smeBusinessName || 'Verified SME Partner'}</span>
	</p>

	<!-- Star Rating & Feedback -->
	{#if experience.isVerified}
		<div class="mt-4 pt-3 border-t border-purple-100/80">
			<div class="flex items-center justify-between mb-2">
				<span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Performance Rating</span>
				<StarRating rating={experience.rating || 5} size="sm" />
			</div>

			{#if experience.feedback}
				<div class="relative mt-2 rounded-xl bg-purple-50/60 p-3.5 border border-purple-100 text-xs text-slate-700 italic leading-relaxed">
					<span class="text-purple-400 font-serif text-lg leading-none absolute top-2 left-2">“</span>
					<p class="pl-3">
						{experience.feedback}
					</p>
				</div>
			{/if}
		</div>
	{:else}
		<div class="mt-4 pt-3 border-t border-slate-100 text-xs text-slate-500 flex items-center gap-2">
			<div class="h-2 w-2 rounded-full bg-amber-400 animate-ping"></div>
			<span>Awaiting SME evaluation and rating to record onto your Experience Ledger.</span>
		</div>
	{/if}
</div>
