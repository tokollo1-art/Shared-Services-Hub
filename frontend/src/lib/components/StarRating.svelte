<script>
	/**
	 * @typedef {Object} Props
	 * @property {number} [rating]
	 * @property {boolean} [interactive]
	 * @property {number} [max]
	 * @property {'sm'|'md'|'lg'} [size]
	 * @property {(newRating: number) => void} [onchange]
	 */

	/** @type {Props} */
	let {
		rating = $bindable(0),
		interactive = false,
		max = 5,
		size = 'md',
		onchange = () => {}
	} = $props();

	let hoverRating = $state(0);

	const starSizes = $derived.by(() => {
		switch (size) {
			case 'sm':
				return 'text-sm gap-0.5';
			case 'lg':
				return 'text-2xl gap-1.5';
			default:
				return 'text-lg gap-1';
		}
	});

	function handleStarClick(index) {
		if (!interactive) return;
		rating = index;
		onchange(index);
	}

	function handleMouseEnter(index) {
		if (!interactive) return;
		hoverRating = index;
	}

	function handleMouseLeave() {
		if (!interactive) return;
		hoverRating = 0;
	}
</script>

<div
	class="inline-flex items-center select-none {starSizes}"
	role={interactive ? 'radiogroup' : 'img'}
	aria-label={`Rating: ${rating} out of ${max} stars`}
	onmouseleave={handleMouseLeave}
>
	{#each Array(max) as _, i}
		{@const starIndex = i + 1}
		{@const activeRating = hoverRating > 0 ? hoverRating : (rating || 0)}
		{@const isFilled = activeRating >= starIndex}
		{@const isHalf = !isFilled && activeRating >= starIndex - 0.5}

		{#if interactive}
			<button
				type="button"
				class="transition-transform hover:scale-125 focus:outline-none focus:ring-2 focus:ring-indigo-500 rounded p-0.5"
				onclick={() => handleStarClick(starIndex)}
				onmouseenter={() => handleMouseEnter(starIndex)}
				title={`${starIndex} star${starIndex > 1 ? 's' : ''}`}
				aria-label={`${starIndex} stars`}
			>
				{#if isFilled}
					<span class="text-amber-400 drop-shadow-xs">★</span>
				{:else}
					<span class="text-slate-300">☆</span>
				{/if}
			</button>
		{:else}
			<span class="transition-colors">
				{#if isFilled}
					<span class="text-amber-400 drop-shadow-xs">★</span>
				{:else if isHalf}
					<span class="text-amber-400">★</span>
				{:else}
					<span class="text-slate-300">☆</span>
				{/if}
			</span>
		{/if}
	{/each}

	{#if rating > 0 && !interactive}
		<span class="ml-1.5 font-semibold text-slate-700 {size === 'sm' ? 'text-xs' : 'text-sm'}">
			{Number(rating).toFixed(1)}
		</span>
	{/if}
</div>
