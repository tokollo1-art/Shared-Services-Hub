<script>
	/**
	 * @typedef {Object} Props
	 * @property {string|null} [message]
	 * @property {() => void} [onretry]
	 * @property {() => void} [ondismiss]
	 * @property {boolean} [dismissible]
	 */

	/** @type {Props} */
	let {
		message = null,
		onretry = null,
		ondismiss = null,
		dismissible = true
	} = $props();

	let dismissed = $state(false);

	function handleDismiss() {
		dismissed = true;
		if (ondismiss) ondismiss();
	}
</script>

{#if message && !dismissed}
	<div
		class="border-l-4 border-[#C42B2B] bg-[#FFF5F5] border-y border-r border-[#F0C0C0] p-4 text-[#0D1B3E] shadow-xs mb-6 flex items-start gap-3.5 transition-all"
		role="alert"
	>
		<div class="text-[#C42B2B] shrink-0 mt-0.5 font-mono font-bold">
			[ERROR]
		</div>

		<div class="flex-1">
			<h4 class="text-xs font-mono font-bold uppercase tracking-wider text-[#C42B2B]">Request Interrupted</h4>
			<p class="mt-1 text-sm text-[#0D1B3E] leading-relaxed">
				{message}
			</p>
			{#if onretry}
				<div class="mt-2.5">
					<button
						type="button"
						class="text-xs font-mono font-bold text-[#C42B2B] hover:underline inline-flex items-center gap-1"
						onclick={onretry}
					>
						<span>[ RETRY ACTION ]</span>
					</button>
				</div>
			{/if}
		</div>

		{#if dismissible}
			<button
				type="button"
				class="text-[#6A84AC] hover:text-[#0D1B3E] p-1 -mr-1 -mt-1"
				onclick={handleDismiss}
				aria-label="Dismiss alert"
			>
				<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
				</svg>
			</button>
		{/if}
	</div>
{/if}
